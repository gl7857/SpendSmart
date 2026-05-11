package com.example.spendsmart;
/**
 * Callback interface for handling responses from Gemini AI requests.
 * This interface is used to receive asynchronous results from the
 * GeminiManager after sending prompts or images for AI processing.
 *
 * @author      Gali Lavi <gl7857@bs.amalnet.k12.il>
 * @version     1.0
 * @since       11/05/2026
 *
 * short description:
 *        This interface defines the communication contract between
 *        the application and the Gemini AI service. It handles the
 *        asynchronous response mechanism by providing two methods:
 *        one for successful AI responses and one for handling errors.
 *        It allows the app to process AI results such as receipt
 *        analysis or text extraction without blocking the main thread.
 */
public interface GeminiCallBack {

    /**
     * Called when the Gemini AI request completes successfully.
     * This method receives the AI-generated result as a String,
     * which may include extracted information or analysis output.
     *
     * @param result the response returned from the AI service
     */
    void onSuccess(String result);

    /**
     * Called when the Gemini AI request fails due to an error.
     * This may include network issues, server errors, or invalid input.
     *
     * @param error the exception describing the failure reason
     */
    void onFailure(Throwable error);
}