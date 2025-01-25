document.addEventListener("DOMContentLoaded", function () {
    const jobsButton = document.getElementById("jobs-button");
    const jobListings = document.querySelector(".job-listings");
    const contactButton = document.getElementById("contact-button");
    const contactDropdown = document.getElementById("contact-dropdown");
    const bubbles = document.querySelectorAll(".bubble");

    contactButton.addEventListener("click", function () {
        contactDropdown.classList.toggle("hidden");
        contactDropdown.classList.toggle("show");
    });

    bubbles.forEach(bubble => {
        bubble.addEventListener('mouseenter', () => {
            bubble.style.transform = 'scale(1.1)';
            bubble.style.boxShadow = '0 0 15px 2px #454cf6';
        });

        bubble.addEventListener('mouseleave', () => {
            bubble.style.transform = 'scale(1)';
            bubble.style.boxShadow = '0 0 10px rgba(0, 0, 0, 0.3)';
        });
    });

    jobsButton.addEventListener("click", function () {
        jobListings.classList.toggle("hidden");
        jobsButton.textContent = jobListings.classList.contains("hidden") ? "Jobs" : "Jobs";
    });

    createCustomCursor();
});

function createCustomCursor() {
    const cursor = document.createElement('div');
    cursor.classList.add('cursor');
    document.body.appendChild(cursor);

    document.addEventListener('mousemove', (e) => {
        const mouseX = e.clientX;
        const mouseY = e.clientY;

        cursor.style.left = `${mouseX}px`;
        cursor.style.top = `${mouseY}px`;
    });
}
