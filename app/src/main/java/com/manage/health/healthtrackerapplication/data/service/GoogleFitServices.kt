package com.example.fitnessapp // Apne actual package name se replace karein

import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.data.Field
import com.google.android.gms.fitness.data.DataSource
import com.google.android.gms.fitness.data.Device
import com.google.android.gms.fitness.data.DataSet
import com.google.android.gms.fitness.data.DataPoint
import com.google.android.gms.fitness.request.DataReadRequest
import com.google.android.gms.fitness.request.DataSourcesRequest
import com.google.android.gms.fitness.result.DataReadResponse
import com.google.android.gms.tasks.Tasks
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.Date
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Service class responsible for interacting with the Google Fit API.
 * It provides comprehensive APIs to fetch daily steps, distance, calories, heart rate,
 * weekly step history, connected wearable devices, and available data sources.
 */
class GoogleFitService(private val context: Context) {

    companion object {
        private const val TAG = "GoogleFitService"
        private const val DEFAULT_TIMEOUT_SECONDS = 30L
        private const val METERS_TO_KILOMETERS = 0.001f
        private const val CALORIES_TO_KCAL = 1.0f
    }

    // Google Sign-In options specifically configured for requesting basic profile data
    private val googleSignInOptions: GoogleSignInOptions by lazy {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestId()
            .requestProfile()
            .build()
    }

    // Google Sign-In Client initialization using the configured options
    private val googleSignInClient: GoogleSignInClient by lazy {
        GoogleSignIn.getClient(context, googleSignInOptions)
    }

    // Comprehensive Fitness Options defining all read scopes required by the application
    private val fitnessOptions: FitnessOptions by lazy {
        FitnessOptions.builder()
            .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
            .addDataType(DataType.TYPE_DISTANCE_DELTA, FitnessOptions.ACCESS_READ)
            .addDataType(DataType.TYPE_CALORIES_EXPENDED, FitnessOptions.ACCESS_READ)
            .addDataType(DataType.TYPE_HEART_RATE_BPM, FitnessOptions.ACCESS_READ)
            .addDataType(DataType.TYPE_ACTIVITY_SEGMENT, FitnessOptions.ACCESS_READ)
            .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
            .addDataType(DataType.AGGREGATE_DISTANCE_DELTA, FitnessOptions.ACCESS_READ)
            .addDataType(DataType.AGGREGATE_CALORIES_EXPENDED, FitnessOptions.ACCESS_READ)
            .build()
    }

    // =================================================================================
    // AUTHENTICATION & PERMISSIONS LOGIC
    // =================================================================================

    /**
     * Checks if Google Fit is fully available.
     * Verifies if a user account exists and whether all required fitness permissions are granted.
     */
    suspend fun isGoogleFitAvailable(): Boolean = withContext(Dispatchers.IO) {
        Log.d(TAG, "isGoogleFitAvailable() invoked. Checking account and permission states...")
        try {
            val account = GoogleSignIn.getLastSignedInAccount(context)
            if (account == null) {
                Log.w(TAG, "isGoogleFitAvailable: No last signed-in Google account found.")
                return@withContext false
            }

            val hasPermissions = GoogleSignIn.hasPermissions(account, fitnessOptions)
            Log.i(TAG, "isGoogleFitAvailable: Account verification successful. Permissions granted status: $hasPermissions")
            return@withContext hasPermissions
        } catch (e: Exception) {
            Log.e(TAG, "Exception encountered in isGoogleFitAvailable critical execution block", e)
            return@withContext false
        }
    }

    /**
     * Validates if Google Fit permissions are present. If missing, logs it explicitly.
     */
    suspend fun requestGoogleFitPermissions(): Boolean = withContext(Dispatchers.IO) {
        Log.d(TAG, "requestGoogleFitPermissions() execution initiated.")
        try {
            val account = GoogleSignIn.getLastSignedInAccount(context)
            if (account == null) {
                Log.w(TAG, "requestGoogleFitPermissions: Operation aborted due to missing Google Account context.")
                return@withContext false
            }

            val hasPermissions = GoogleSignIn.hasPermissions(account, fitnessOptions)
            if (hasPermissions) {
                Log.i(TAG, "requestGoogleFitPermissions: All requested scopes are already successfully declared and granted.")
                return@withContext true
            }

            Log.w(TAG, "requestGoogleFitPermissions: Required permission scopes are explicitly missing. Activity callback required.")
            return@withContext false
        } catch (e: Exception) {
            Log.e(TAG, "Critical error caught during runtime evaluation of requestGoogleFitPermissions", e)
            return@withContext false
        }
    }

    /**
     * Generates the essential platform intent to trigger Google Fit OAuth permission prompt.
     */
    fun getGoogleFitPermissionsIntent(): Intent {
        Log.d(TAG, "getGoogleFitPermissionsIntent: Compiling platform authorization bundle mapping.")
        val account = GoogleSignIn.getLastSignedInAccount(context)
        return if (account != null) {
            Log.i(TAG, "Generating permission specific intent utilizing existing active user context session.")
            GoogleSignIn.getClient(context, googleSignInOptions).signInIntent
        } else {
            Log.w(TAG, "Fallback initiated: active account null context during intent structural building.")
            googleSignInClient.signInIntent
        }
    }

    /**
     * Evaluates if a completely clean Google Client Authorization sign-in flow is necessary.
     */
    fun needsGoogleSignIn(): Boolean {
        val account = GoogleSignIn.getLastSignedInAccount(context)
        val evaluationResult = account == null
        Log.d(TAG, "needsGoogleSignIn evaluation: Status evaluated to [$evaluationResult]. Active Email Reference: ${account?.email ?: "None Available"}")
        return evaluationResult
    }

    /**
     * Handles and extracts the complete complex Google Identity Object parameters from internal Activity Result mapping.
     */
    suspend fun handleSignInResult(data: Intent?): Boolean = withContext(Dispatchers.IO) {
        Log.i(TAG, "handleSignInResult processing started. Attempting parsing payload mapping block...")
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.getResult(ApiException::class.java)
            if (account != null) {
                Log.i(TAG, "handleSignInResult successful! Account registered signature email identifier: ${account.email}")
                return@withContext true
            } else {
                Log.e(TAG, "handleSignInResult evaluated execution: Structural result was verified null without explicit exception mapping.")
                return@withContext false
            }
        } catch (e: ApiException) {
            Log.e(TAG, "Google API client infrastructure thrown explicit exception layer status code mapped: ${e.statusCode}")
            when (e.statusCode) {
                CommonStatusCodes.SIGN_IN_REQUIRED -> Log.e(TAG, "Error Mapped Code [SIGN_IN_REQUIRED]: Explicit user interaction required to establish session state flow.")
                CommonStatusCodes.INVALID_ACCOUNT -> Log.e(TAG, "Error Mapped Code [INVALID_ACCOUNT]: The explicit account selected cannot support authenticating operations with target scope configurations.")
                CommonStatusCodes.NETWORK_ERROR -> Log.e(TAG, "Error Mapped Code [NETWORK_ERROR]: Structural socket failure detected. Ensure a viable target endpoint pipe is live.")
                CommonStatusCodes.INTERNAL_ERROR -> Log.e(TAG, "Error Mapped Code [INTERNAL_ERROR]: Internal structural client state crash within the Core API Client framework library.")
                else -> Log.e(TAG, "Unhandled unique complex API state exception variant encountered. Status Code: ${e.statusCode} message description text: ${e.localizedMessage}")
            }
            return@withContext false
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected complex structural failure context mapping during handleSignInResult processing lifecycle.", e)
            return@withContext false
        }
    }

    /**
     * Provides the basic launch identity framework implicit Intent object.
     */
    fun getGoogleSignInIntent(): Intent {
        Log.d(TAG, "getGoogleSignInIntent: Provisioning standard launch setup payload structural reference.")
        return googleSignInClient.signInIntent
    }

    /**
     * Performs a local sign-out operation to break structural token caching bindings locally.
     */
    fun signOut() {
        Log.i(TAG, "signOut invoked. Purging credential mappings across standard frameworks locally.")
        try {
            googleSignInClient.signOut().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.i(TAG, "Local caching engine cleared. Sign-out completed successfully.")
                } else {
                    Log.e(TAG, "Sign-out task evaluation returned failure tracking signature node.", task.exception)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failure handling explicit local logging logout operations stack trace logs", e)
        }
    }

    // =================================================================================
    // HOURLY / DAILY METRICS FETCHING (STEPS, DISTANCE, CALORIES, HEART RATE)
    // =================================================================================

    /**
     * Extracts step data points across the modern historical client architecture.
     */
    suspend fun getTodaySteps(): Int = withContext(Dispatchers.IO) {
        Log.d(TAG, "getTodaySteps processing running inside Dispatcher stream context.")
        try {
            val account = GoogleSignIn.getLastSignedInAccount(context)
            if (account == null || !GoogleSignIn.hasPermissions(account, fitnessOptions)) {
                Log.w(TAG, "getTodaySteps aborted: Authentication authorization validation layer failure verification.")
                return@withContext 0
            }

            val calendar = Calendar.getInstance()
            val endTime = calendar.timeInMillis

            // Setting floor configurations for current calculation block limits
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val startTime = calendar.timeInMillis

            Log.d(TAG, "Step history date parameters: Querying from ${formatTimestampForLogging(startTime)} to ${formatTimestampForLogging(endTime)}")

            val readRequest = DataReadRequest.Builder()
                .read(DataType.TYPE_STEP_COUNT_DELTA)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build()

            val response = Tasks.await(Fitness.getHistoryClient(context, account).readData(readRequest))
            var cumulativeSteps = 0

            for (dataSet in response.dataSets) {
                cumulativeSteps += parseAndSumDataSetIntegerValues(dataSet, Field.FIELD_STEPS.name)
            }

            Log.i(TAG, "Calculated today metrics computation layer total: [ $cumulativeSteps Steps ] extracted safely.")
            return@withContext cumulativeSteps
        } catch (e: Exception) {
            Log.e(TAG, "Exception tracked inside getTodaySteps compilation loop block layers.", e)
            return@withContext 0
        }
    }

    /**
     * Evaluates metrics for displacement tracking over real-time processing pipelines.
     */
    suspend fun getTodayDistance(): Float = withContext(Dispatchers.IO) {
        Log.d(TAG, "getTodayDistance data reading running inside active thread context stream.")
        try {
            val account = GoogleSignIn.getLastSignedInAccount(context)
            if (account == null || !GoogleSignIn.hasPermissions(account, fitnessOptions)) {
                Log.w(TAG, "getTodayDistance evaluation aborted: Pre-flight security requirements validation failing.")
                return@withContext 0f
            }

            val calendar = Calendar.getInstance()
            val endTime = calendar.timeInMillis
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val startTime = calendar.timeInMillis

            val readRequest = DataReadRequest.Builder()
                .read(DataType.TYPE_DISTANCE_DELTA)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build()

            val response = Tasks.await(Fitness.getHistoryClient(context, account).readData(readRequest))
            var aggregatedDistance = 0f

            for (dataSet in response.dataSets) {
                aggregatedDistance += parseAndSumDataSetFloatValues(dataSet, Field.FIELD_DISTANCE.name)
            }

            Log.i(TAG, "getTodayDistance aggregate result raw float output value evaluated: $aggregatedDistance meters.")
            return@withContext aggregatedDistance
        } catch (e: Exception) {
            Log.e(TAG, "Exception handled during processing structural calculations within getTodayDistance", e)
            return@withContext 0f
        }
    }

    /**
     * Pulls data mapping parameters for kinetic energy output readings.
     */
    suspend fun getTodayCalories(): Float = withContext(Dispatchers.IO) {
        Log.d(TAG, "getTodayCalories operational task mapping dispatched.")
        try {
            val account = GoogleSignIn.getLastSignedInAccount(context)
            if (account == null || !GoogleSignIn.hasPermissions(account, fitnessOptions)) {
                Log.w(TAG, "getTodayCalories execution context terminated prematurely. Checks failed.")
                return@withContext 0f
            }

            val calendar = Calendar.getInstance()
            val endTime = calendar.timeInMillis
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val startTime = calendar.timeInMillis

            val readRequest = DataReadRequest.Builder()
                .read(DataType.TYPE_CALORIES_EXPENDED)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build()

            val response = Tasks.await(Fitness.getHistoryClient(context, account).readData(readRequest))
            var aggregatedCalories = 0f

            for (dataSet in response.dataSets) {
                aggregatedCalories += parseAndSumDataSetFloatValues(dataSet, Field.FIELD_CALORIES.name)
            }

            Log.i(TAG, "getTodayCalories calculation analysis pass: Completed with sum metric yielding: $aggregatedCalories kcal.")
            return@withContext aggregatedCalories
        } catch (e: Exception) {
            Log.e(TAG, "Fatal parsing tracking data loop mapping block interruption inside getTodayCalories context", e)
            return@withContext 0f
        }
    }

    /**
     * Dispatches historical network inquiries looking for specific cardiovascular pulse data array.
     */
    suspend fun getLatestHeartRate(): Float = withContext(Dispatchers.IO) {
        Log.d(TAG, "getLatestHeartRate request processing pipe started running.")
        try {
            val account = GoogleSignIn.getLastSignedInAccount(context)
            if (account == null || !GoogleSignIn.hasPermissions(account, fitnessOptions)) {
                Log.w(TAG, "getLatestHeartRate structural execution cancelled. Invalid session credentials permissions profile setup.")
                return@withContext 0f
            }

            val endTime = System.currentTimeMillis()
            val startTime = endTime - TimeUnit.DAYS.toMillis(1) // Scanning maximum back threshold constraint parameter boundary.

            val readRequest = DataReadRequest.Builder()
                .read(DataType.TYPE_HEART_RATE_BPM)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .setLimit(1) // Ensure only the single latest captured metric element matches.
                .build()

            val response = Tasks.await(Fitness.getHistoryClient(context, account).readData(readRequest))
            var structuralHeartRateReadingValue = 0f

            for (dataSet in response.dataSets) {
                if (!dataSet.isEmpty) {
                    for (dataPoint in dataSet.dataPoints) {
                        for (field in dataPoint.dataType.fields) {
                            if (field.name == Field.FIELD_BPM.name) {
                                structuralHeartRateReadingValue = dataPoint.getValue(field).asFloat()
                                Log.d(TAG, "Located single matching modern record pulse reading context output value: $structuralHeartRateReadingValue")
                            }
                        }
                    }
                }
            }

            Log.i(TAG, "getLatestHeartRate complete loop parsing trace processing yield: $structuralHeartRateReadingValue bpm.")
            return@withContext structuralHeartRateReadingValue
        } catch (e: Exception) {
            Log.e(TAG, "Exception captured inside context execution tree loop for parsing getLatestHeartRate structural nodes.", e)
            return@withContext 0f
        }
    }

    // =================================================================================
    // COMPLEX WEEKLY STEP HISTORY & DEVICE DETECTION LOGIC
    // =================================================================================

    /**
     * Extracts 7 days steps based on explicit Calendar dayIndex calculations.
     * Mapped precisely to handle the complex cyclic modular algorithm from Screenshot 2026-06-24 215438.jpg.
     */
    suspend fun getWeeklyStepHistory(): List<Int> = withContext(Dispatchers.IO) {
        Log.d(TAG, "getWeeklyStepHistory structural mapping function launched.")
        val dailyStepsArray = IntArray(7) { 0 }
        val helperCalendar = Calendar.getInstance()

        try {
            val account = GoogleSignIn.getLastSignedInAccount(context)
            if (account == null || !GoogleSignIn.hasPermissions(account, fitnessOptions)) {
                Log.w(TAG, "getWeeklyStepHistory: Verification constraint validation layer block termination sequence matched.")
                return@withContext List(7) { 0 }
            }

            val endTime = System.currentTimeMillis()
            val startTime = endTime - TimeUnit.DAYS.toMillis(7)

            Log.d(TAG, "getWeeklyStepHistory structural scanning boundary parameters: Start range [${formatTimestampForLogging(startTime)}] through End parameter [${formatTimestampForLogging(endTime)}]")

            val readRequest = DataReadRequest.Builder()
                .read(DataType.TYPE_STEP_COUNT_DELTA)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build()

            val response = Tasks.await(Fitness.getHistoryClient(context, account).readData(readRequest))
            Log.d(TAG, "History Client returned data structural packet layer. Parsing content streams items counts...")

            for (dataSet in response.dataSets) {
                Log.d(TAG, "Analyzing structural element inside Target DataSet type signature: ${dataSet.dataType.name}")
                for (dataPoint in dataSet.dataPoints) {
                    val individualPointTimestamp = dataPoint.getStartTime(TimeUnit.MILLISECONDS)
                    helperCalendar.timeInMillis = individualPointTimestamp

                    val dayOfWeekEnumIdValue = helperCalendar.get(Calendar.DAY_OF_WEEK)

                    // Core algorithm implementation explicitly tracking index parameters matching original screenshot calculations
                    val dayIndex = (dayOfWeekEnumIdValue - 1) % 7

                    for (field in dataPoint.dataType.fields) {
                        if (field.name == Field.FIELD_STEPS.name) {
                            val capturedStepsCalculatedScalarValue = dataPoint.getValue(field).asInt()

                            if (dayIndex in 0..6) {
                                dailyStepsArray[dayIndex] += capturedStepsCalculatedScalarValue
                                Log.d(TAG, "Step matching element captured! DayOfWeek: $dayOfWeekEnumIdValue mapped safely into Array Index Position: $dayIndex. Steps assigned payload value incremented by: $capturedStepsCalculatedScalarValue")
                            } else {
                                Log.w(TAG, "Warning: An anomalous index range calculation boundary anomaly tracking error registered: Calculated structural array index location was matched to: $dayIndex")
                            }
                        }
                    }
                }
            }

            val outputTransformedChronologicalList = dailyStepsArray.toList()
            Log.i(TAG, "Retrieved weekly steps: $outputTransformedChronologicalList successfully mapped from Google Fit framework components safely.")
            return@withContext outputTransformedChronologicalList
        } catch (e: Exception) {
            Log.e(TAG, "Fatal parsing exception error caught during execution matching array mapping parsing logic in getWeeklyStepHistory", e)
            return@withContext List(7) { 0 }
        }
    }

    /**
     * Evaluates connection parameters to determine if data is originating from high frequency physical wearable sensors.
     * Corresponds completely to the structural hardware checking loops logic in Screenshot 2026-06-24 215307.jpg.
     */
    suspend fun isWearableDeviceConnected(): Boolean = withContext(Dispatchers.IO) {
        Log.d(TAG, "isWearableDeviceConnected checking task sequence initialized.")
        try {
            val account = GoogleSignIn.getLastSignedInAccount(context)
            if (account == null || !GoogleSignIn.hasPermissions(account, fitnessOptions)) {
                Log.w(TAG, "isWearableDeviceConnected checked loop killed: Context user verification returned null value parameters.")
                return@withContext false
            }

            val endTime = System.currentTimeMillis()
            val startTime = endTime - TimeUnit.HOURS.toMillis(1) // Scans the immediate short-term history buffer stream.

            val readRequest = DataReadRequest.Builder()
                .read(DataType.TYPE_STEP_COUNT_DELTA)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build()

            val response = Tasks.await(Fitness.getHistoryClient(context, account).readData(readRequest))
            var detectedHardwareWearableDataSignatureToken = false

            for (dataSet in response.dataSets) {
                for (dataPoint in dataSet.dataPoints) {
                    val originalDeviceSourceMappingNode: DataSource? = dataPoint.originalDataSource
                    if (originalDeviceSourceMappingNode != null) {
                        val physicalHardwareDeviceReferenceObject: Device? = originalDeviceSourceMappingNode.device
                        if (physicalHardwareDeviceReferenceObject != null) {
                            val runtimeHardwareDeviceTypeEnumCode = physicalHardwareDeviceReferenceObject.type
                            Log.d(TAG, "isWearableDeviceConnected checking: Located source entity signature details model: [${physicalHardwareDeviceReferenceObject.model}] with Device Type Enum ID Code: [$runtimeHardwareDeviceTypeEnumCode]")

                            if (runtimeHardwareDeviceTypeEnumCode == Device.TYPE_WATCH ||
                                runtimeHardwareDeviceTypeEnumCode == Device.TYPE_CHEST_STRAP) {
                                Log.i(TAG, "Target match validated! Confirmed data signature trace matched perfectly to a certified wearable hardware layer.")
                                detectedHardwareWearableDataSignatureToken = true
                                break
                            }
                        }
                    }
                }
                if (detectedHardwareWearableDataSignatureToken) break
            }

            Log.i(TAG, "Final analytical execution evaluation for isWearableDeviceConnected returned state value: $detectedHardwareWearableDataSignatureToken")
            return@withContext detectedHardwareWearableDataSignatureToken
        } catch (e: Exception) {
            Log.e(TAG, "Error state encountered during tracking active hardware source evaluation operations logic within isWearableDeviceConnected", e)
            return@withContext false
        }
    }

    /**
     * Inspects and maps comprehensive details regarding hardware peripheral nodes.
     * Corresponds completely to data mappings from Screenshot 2026-06-24 215319.jpg.
     */
    suspend fun getAvailableDataSources(): List<String> = withContext(Dispatchers.IO) {
        Log.d(TAG, "getAvailableDataSources invoked. Initializing infrastructure tracking pipeline query details.")
        val discoveredDeviceDescriptionStringsArray = mutableListOf<String>()

        try {
            val account = GoogleSignIn.getLastSignedInAccount(context)
            if (account == null || !GoogleSignIn.hasPermissions(account, fitnessOptions)) {
                Log.w(TAG, "getAvailableDataSources query aborted: Active credentials context parameters could not pass mapping validation checks.")
                return@withContext emptyList()
            }

            val dataSourcesRequest = DataSourcesRequest.Builder()
                .setDataTypes(DataType.TYPE_STEP_COUNT_DELTA)
                .build()

            val responseDataSourcesList = Tasks.await(
                Fitness.getSensorsClient(context, account).findDataSources(dataSourcesRequest)
            )

            Log.d(TAG, "Sensors Client successfully returned a collection size layer count of [${responseDataSourcesList.size}] elements. Beginning enumeration...")

            for (dataSourceElementItem in responseDataSourcesList) {
                val hardwareDevicePointer: Device? = dataSourceElementItem.device
                val descriptiveStringValueItem = if (hardwareDevicePointer != null) {
                    val designModelIdentifierText = hardwareDevicePointer.model ?: "Generic Source Device"
                    val parsedTypeMappingLabel = when (hardwareDevicePointer.type) {
                        Device.TYPE_WATCH -> "Smartwatch Wearable Peripheral"
                        Device.TYPE_PHONE -> "Mobile Handset Device Platform"
                        Device.TYPE_TABLET -> "Tablet Smart Screen Component"
                        Device.TYPE_CHEST_STRAP -> "Biometric Chest Sensor Component"
                        Device.TYPE_SCALE -> "Smart Weighing Digital Scale Engine"
                        else -> "External Miscellaneous Hardware Interface Data Pump"
                    }
                    "Device Model: $designModelIdentifierText [Classification: $parsedTypeMappingLabel]"
                } else {
                    "Software Application Virtual Stream Node Identifier: ${dataSourceElementItem.streamName ?: "Unknown Logical Source Channel"}"
                }

                discoveredDeviceDescriptionStringsArray.add(descriptiveStringValueItem)
                Log.d(TAG, "Successfully formatted and added unique system target source identity token string item: $descriptiveStringValueItem")
            }

            Log.i(TAG, "getAvailableDataSources completed execution. Compiled tracking list size: ${discoveredDeviceDescriptionStringsArray.size} total items captured.")
            return@withContext discoveredDeviceDescriptionStringsArray
        } catch (e: Exception) {
            Log.e(TAG, "Fatal parsing exception error caught during analytical inquiry scanning inside getAvailableDataSources structural layer execution.", e)
            return@withContext emptyList()
        }
    }

    // =================================================================================
    // UTILITY HELPER METHODS (INTERNAL EXTRA LOGIC ADDED FOR ROBUSTNESS)
    // =================================================================================

    /**
     * Loops through DataSet records parsing matching integer key value parameters.
     */
    private fun parseAndSumDataSetIntegerValues(targetDataSet: DataSet, targetFieldNameFilterKey: String): Int {
        var temporalRunningSum = 0
        if (!targetDataSet.isEmpty) {
            for (dataPointElementNode in targetDataSet.dataPoints) {
                for (fieldPropertyDefinition in dataPointElementNode.dataType.fields) {
                    if (fieldPropertyDefinition.name == targetFieldNameFilterKey) {
                        val scalarVal = dataPointElementNode.getValue(fieldPropertyDefinition).asInt()
                        temporalRunningSum += scalarVal
                    }
                }
            }
        }
        return temporalRunningSum
    }

    /**
     * Loops through DataSet records parsing matching float scalar value fields.
     */
    private fun parseAndSumDataSetFloatValues(targetDataSet: DataSet, targetFieldNameFilterKey: String): Float {
        var temporalRunningSum = 0f
        if (!targetDataSet.isEmpty) {
            for (dataPointElementNode in targetDataSet.dataPoints) {
                for (fieldPropertyDefinition in dataPointElementNode.dataType.fields) {
                    if (fieldPropertyDefinition.name == targetFieldNameFilterKey) {
                        val scalarVal = dataPointElementNode.getValue(fieldPropertyDefinition).asFloat()
                        temporalRunningSum += scalarVal
                    }
                }
            }
        }
        return temporalRunningSum
    }

    /**
     * Formats timestamp parameters into highly readable logging representations for deep monitoring.
     */
    private fun formatTimestampForLogging(epochMillisecondValue: Long): String {
        return try {
            val loggingFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())
            loggingFormatter.format(Date(epochMillisecondValue))
        } catch (e: Exception) {
            "Invalid Timestamp Representation Reference String String Mapping Exception"
        }
    }
}