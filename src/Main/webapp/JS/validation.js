// Regular expressions for validation
const emailRegex = /^(([^<>()\[\]\\.,;:\s@"]+(\.[^<>()\[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;

// Event listener for the login form submission
document.getElementById("loginForm").addEventListener("submit", async function (event) {
    event.preventDefault(); // Prevent default form submission

    const email = this.querySelector('input[type="email"]').value;
    const password = this.querySelector('input[type="password"]').value;

    let valid = true;

    // Validate email format
    if (!emailRegex.test(email)) {
        alert("Please enter a valid email.");
        valid = false;
    }

    // Proceed to server-side validation if input is valid
    if (valid) {
        try {
            const response = await fetch("/UserServlet", {
                method: "POST",
                headers: {
                    "Content-Type": "application/x-www-form-urlencoded",
                },
                body: `email=${encodeURIComponent(email)}&password=${encodeURIComponent(password)}`,
            });

            if (response.ok) {
                const data = await response.json();
                if (data.status === "success") {
                    alert(data.message); // Display success message
                    window.location.href = "Homepage.html"; // Redirect to the homepage
                }
            } else {
                const errorData = await response.json();
                alert(errorData.message); // Display error message
            }
        } catch (error) {
            console.error("Error during login:", error);
            alert("Check if you're Signed in or not");
        }
    }
});

// Event listener for the sign-up form submission
document.getElementById("signUpForm").addEventListener("submit", function (event) {
    event.preventDefault(); // Prevent default form submission

    const username = this.querySelector('input[type="text"]').value;
    const email = this.querySelector('input[type="email"]').value;
    const password = this.querySelector('input[type="password"]').value;

    let valid = true;

    // Validate username is not empty
    if (username.trim() === "") {
        alert("Username is required.");
        valid = false;
    }

    // Validate email format
    if (!emailRegex.test(email)) {
        alert("Please enter a valid email.");
        valid = false;
    }

    // Validate password length (client-side only for sign-up)
    if (password.length < 8) {
        alert("Password must be at least 8 characters long.");
        valid = false;
    }

    if (valid) {
        alert("Sign-up successful!");
        // Additional sign-up processing can be added here
    }
});
