document.querySelectorAll(".drop-zone__input").forEach((inputElement) => {
    const dropZoneElement = inputElement.closest(".drop-zone");

    dropZoneElement.addEventListener("click", () => inputElement.click());

    inputElement.addEventListener("change", (e) => {
        if (inputElement.files.length) {
            handleFile(inputElement.files);
        }
    });

    dropZoneElement.addEventListener("dragover", (e) => {
        e.preventDefault();
        dropZoneElement.classList.add("drop-zone--over");
    });

    ["dragleave", "dragend"].forEach((type) => {
        dropZoneElement.addEventListener(type, () => {
            dropZoneElement.classList.remove("drop-zone--over");
        });
    });

    dropZoneElement.addEventListener("drop", (e) => {
        e.preventDefault();
        if (e.dataTransfer.files.length) {
            inputElement.files = e.dataTransfer.files;
            handleFile(e.dataTransfer.files);
        }
        dropZoneElement.classList.remove("drop-zone--over");
    });
});

// Handles file selection or drop
function handleFile(files) {
    updateHiddenInput(files);
    uploadFile(); // Automatically submit the form
}

// Update the hidden file input for form submission
function updateHiddenInput(files) {
    const hiddenInput = document.getElementById("hiddenFileInput");
    const dataTransfer = new DataTransfer();
    for (let i = 0; i < files.length; i++) {
        dataTransfer.items.add(files[i]);
    }
    hiddenInput.files = dataTransfer.files;
}

// Automatically submit the form
function uploadFile() {
    const form = document.getElementById("uploadForm");
    form.submit();
}
