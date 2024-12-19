package com.applications.bobatea.customExceptions;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String handleMaxUploadSizeExceededException(MaxUploadSizeExceededException exc, Model model) {
        // Log the exception if necessary
        // exc.printStackTrace();  // Optional: log it

        // Add a custom error message to the model
        model.addAttribute("message", "File size exceeds the maximum allowed limit of 1MB.");

        // Return the name of the custom error page
        return "error-page";  // Replace with your custom error view
    }

    @ExceptionHandler(MinioClientException.class)
    public String handleMinioClientException(MinioClientException exc, Model model) {
        exc.printStackTrace();
        model.addAttribute("message", "Minio Client error occurred: " + exc.getMessage());
        return "error-page"; // Your custom error page
    }

    // Handle IllegalArgumentException
    @ExceptionHandler(IllegalArgumentException.class)
    public String handleIllegalArgumentException(IllegalArgumentException exc, Model model) {
        exc.printStackTrace();
        model.addAttribute("message", "Invalid argument: " + exc.getMessage());
        return "error-page"; // Your custom error page
    }

    // Handle UsernameNotFoundException
    @ExceptionHandler(UsernameNotFoundException.class)
    public String handleUsernameNotFoundException(UsernameNotFoundException exc, Model model) {
        exc.printStackTrace();
        model.addAttribute("message", "User not found.");
        return "error-page"; // Your custom error page
    }

    // Handle any other unexpected exceptions
    @ExceptionHandler(Exception.class)
    public String handleGenericException(Exception exc, Model model) {
        exc.printStackTrace();
        model.addAttribute("message", "An unexpected error occurred.");
        return "error-page"; // Your custom error page
    }

}
