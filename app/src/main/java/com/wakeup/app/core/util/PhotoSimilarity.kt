package com.wakeup.app.core.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.abs

/**
 * Utility class for computing perceptual hashes and similarity between images.
 * Uses dHash (difference hash) algorithm for fast similarity comparison.
 */
object PhotoSimilarity {

    private const val TAG = "PhotoSimilarity"
    private const val HASH_SIZE = 8 // 8x8 = 64 bit hash
    private const val SIMILARITY_THRESHOLD = 0.85f // 85% similarity required

    /**
     * Compute dHash (difference hash) for an image file
     * @param imageFile The image file to hash
     * @return Hex string representation of the hash, or null if failed
     */
    suspend fun computeHash(imageFile: File): String? = withContext(Dispatchers.Default) {
        try {
            // Load and resize image
            val bitmap = loadAndResizeBitmap(imageFile) ?: return@withContext null

            // Compute hash
            val hash = computeDHash(bitmap)
            bitmap.recycle()

            hash
        } catch (e: Exception) {
            Log.e(TAG, "Failed to compute hash", e)
            null
        }
    }

    /**
     * Compare two images for similarity
     * @param referencePath Path to reference image
     * @param capturedPath Path to captured image
     * @return Similarity score between 0.0 and 1.0, or null if comparison failed
     */
    suspend fun compareImages(referencePath: String, capturedPath: String): Float? = withContext(Dispatchers.Default) {
        try {
            val referenceFile = File(referencePath)
            val capturedFile = File(capturedPath)

            if (!referenceFile.exists() || !capturedFile.exists()) {
                Log.e(TAG, "One or both image files don't exist")
                return@withContext null
            }

            val hash1 = computeHash(referenceFile) ?: return@withContext null
            val hash2 = computeHash(capturedFile) ?: return@withContext null

            compareHashes(hash1, hash2)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to compare images", e)
            null
        }
    }

    /**
     * Compare two hash strings
     * @param hash1 First hash (hex string)
     * @param hash2 Second hash (hex string)
     * @return Similarity score between 0.0 and 1.0
     */
    fun compareHashes(hash1: String, hash2: String): Float {
        if (hash1.length != hash2.length) return 0.0f

        val bits1 = hexToBinary(hash1)
        val bits2 = hexToBinary(hash2)

        if (bits1.length != bits2.length) return 0.0f

        // Count matching bits
        var matchingBits = 0
        for (i in bits1.indices) {
            if (bits1[i] == bits2[i]) {
                matchingBits++
            }
        }

        return matchingBits.toFloat() / bits1.length
    }

    /**
     * Check if two images are similar enough to be considered a match
     * @param similarity Similarity score from compareImages
     * @return true if images are similar enough
     */
    fun isMatch(similarity: Float?): Boolean {
        return similarity != null && similarity >= SIMILARITY_THRESHOLD
    }

    /**
     * Copy image to internal storage and compute hash
     * @param sourceUri Source image URI or path
     * @param destinationDir Destination directory
     * @return Pair of (file path, hash) or null if failed
     */
    suspend fun copyAndHashImage(
        sourcePath: String,
        destinationDir: File
    ): Pair<String, String>? = withContext(Dispatchers.IO) {
        try {
            val sourceFile = File(sourcePath)
            if (!sourceFile.exists()) {
                Log.e(TAG, "Source file doesn't exist: $sourcePath")
                return@withContext null
            }

            // Generate unique filename
            val destFile = File(destinationDir, "ref_${System.currentTimeMillis()}.jpg")

            // Copy and downsample image to save space
            copyAndDownsample(sourceFile, destFile)

            // Compute hash
            val hash = computeHash(destFile) ?: run {
                destFile.delete()
                return@withContext null
            }

            Pair(destFile.absolutePath, hash)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to copy and hash image", e)
            null
        }
    }

    /**
     * Load bitmap and resize to hash size
     */
    private fun loadAndResizeBitmap(file: File): Bitmap? {
        return try {
            // Decode bounds only first
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeFile(file.absolutePath, options)

            // Calculate sample size to downsample
            options.inSampleSize = calculateInSampleSize(options, HASH_SIZE + 1, HASH_SIZE)
            options.inJustDecodeBounds = false

            // Decode with sample size
            var bitmap = BitmapFactory.decodeFile(file.absolutePath, options)
                ?: return null

            // Resize to exact dimensions needed for hash
            if (bitmap.width != HASH_SIZE + 1 || bitmap.height != HASH_SIZE) {
                val scaled = Bitmap.createScaledBitmap(bitmap, HASH_SIZE + 1, HASH_SIZE, true)
                bitmap.recycle()
                bitmap = scaled
            }

            bitmap
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load bitmap", e)
            null
        }
    }

    /**
     * Compute dHash from bitmap
     * Algorithm: Compare adjacent pixels horizontally, 1 if right > left, 0 otherwise
     */
    private fun computeDHash(bitmap: Bitmap): String {
        val hashBits = StringBuilder()

        for (y in 0 until HASH_SIZE) {
            for (x in 0 until HASH_SIZE) {
                val leftPixel = bitmap.getPixel(x, y)
                val rightPixel = bitmap.getPixel(x + 1, y)

                val leftIntensity = pixelIntensity(leftPixel)
                val rightIntensity = pixelIntensity(rightPixel)

                // 1 if right is brighter, 0 otherwise
                hashBits.append(if (rightIntensity > leftIntensity) '1' else '0')
            }
        }

        // Convert binary string to hex
        return binaryToHex(hashBits.toString())
    }

    /**
     * Calculate pixel intensity (grayscale value)
     */
    private fun pixelIntensity(pixel: Int): Int {
        val r = Color.red(pixel)
        val g = Color.green(pixel)
        val b = Color.blue(pixel)
        return (r + g + b) / 3
    }

    /**
     * Calculate inSampleSize for BitmapFactory
     */
    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2

            while ((halfHeight / inSampleSize) >= reqHeight &&
                (halfWidth / inSampleSize) >= reqWidth
            ) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }

    /**
     * Copy and downsample image to reduce storage size
     * Uses inJustDecodeBounds to calculate proper sample size before loading
     */
    private fun copyAndDownsample(source: File, destination: File) {
        // First decode bounds to get image dimensions
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeFile(source.absolutePath, options)
        
        // Calculate sample size to downsample to ~2MP max (safe for memory)
        val maxDimension = 2048
        options.inSampleSize = calculateInSampleSize(options, maxDimension, maxDimension)
        options.inJustDecodeBounds = false
        
        val bitmap = BitmapFactory.decodeFile(source.absolutePath, options)
            ?: throw IllegalStateException("Failed to decode source image")

        // Compress and save
        destination.outputStream().use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
        }

        bitmap.recycle()
    }

    /**
     * Convert binary string to hex string
     */
    private fun binaryToHex(binary: String): String {
        val hex = StringBuilder()
        for (i in binary.indices step 4) {
            val chunk = binary.substring(i, minOf(i + 4, binary.length))
            val decimal = chunk.padEnd(4, '0').toInt(2)
            hex.append(decimal.toString(16))
        }
        return hex.toString()
    }

    /**
     * Convert hex string to binary string
     */
    private fun hexToBinary(hex: String): String {
        val binary = StringBuilder()
        for (char in hex) {
            val decimal = char.toString().toInt(16)
            binary.append(decimal.toString(2).padStart(4, '0'))
        }
        return binary.toString()
    }

    /**
     * Clean up old reference images (call periodically)
     */
    fun cleanupOldReferences(directory: File, maxAgeMs: Long = 30 * 24 * 60 * 60 * 1000L) {
        // Delete files older than 30 days
        val cutoff = System.currentTimeMillis() - maxAgeMs
        directory.listFiles()?.forEach { file ->
            if (file.lastModified() < cutoff) {
                file.delete()
            }
        }
    }
}
