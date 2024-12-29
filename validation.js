// Email validation regex
const emailRegex = /^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,6}$/;

// Password strength check (at least 8 characters, 1 uppercase, 1 lowercase, 1 number)
const passwordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)[A-Za-z\d]{8,}$/;

// Login form validation
document.getElementById("loginForm").addEventListener("submit", function(event) {
    event.preventDefault(); // Prevent form submission

    const email = this.querySelector('input[type="email"]').value;
    const password = this.querySelector('input[type="password"]').value;

    let valid = true;

    // Validate email
    if (!emailRegex.test(email)) {
        alert("Please enter a valid email.");
        valid = false;
    }

    // Validate password
    if (password.length < 6) {
        alert("Password must be at least 6 characters long.");
        valid = false;
    }

    if (valid) {
        alert("Login successful!");
        // Proceed with form submission (e.g., redirect or send data)
    }
});

// Sign-up form validation
document.getElementById("signUpForm").addEventListener("submit", function(event) {
    event.preventDefault(); // Prevent form submission

    const username = this.querySelector('input[type="text"]').value;
    const email = this.querySelector('input[type="email"]').value;
    const password = this.querySelector('input[type="password"]').value;

    let valid = true;

    // Validate username
    if (username.trim() === "") {
        alert("Username is required.");
        valid = false;
    }

    // Validate email
    if (!emailRegex.test(email)) {
        alert("Please enter a valid email.");
        valid = false;
    }

    // Validate password
    if (!passwordRegex.test(password)) {
        alert("Password must be at least 8 characters, with 1 uppercase, 1 lowercase, and 1 number.");
        valid = false;
    }

    if (valid) {
        alert("Sign-up successful!");
        // Proceed with form submission (e.g., save data, redirect)
    }
});
