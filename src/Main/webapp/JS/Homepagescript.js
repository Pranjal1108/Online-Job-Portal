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
        if (jobListings.classList.contains("hidden")) {
            jobsButton.textContent = "Jobs";
        } else {
            jobsButton.textContent = "Jobs";
        }
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

    function storeAppliedJobs(jobId) {
        const appliedJobs = JSON.parse(localStorage.getItem('appliedJobs')) || [];
        appliedJobs.push(jobId);
        localStorage.setItem('appliedJobs', JSON.stringify(appliedJobs));
    }


    function displayAppliedJobs() {
        const appliedJobsList = document.getElementById('applied-jobs-list');
        const appliedJobs = JSON.parse(localStorage.getItem('appliedJobs')) || [];
        appliedJobsList.innerHTML = '';
        appliedJobs.forEach((jobId) => {
            // Assume jobIds are stored in an object with job details
            const job = getJobDetails(jobId);
            const li = document.createElement('li');
            li.textContent = job.title;
            appliedJobsList.appendChild(li);
        });
    }

    function getJobDetails(jobId) {
        return {title: 'Job Title'};
    }

    document.addEventListener('DOMContentLoaded', displayAppliedJobs);

    function submitJobApplication(jobId) {
        storeAppliedJobs(jobId);
        displayAppliedJobs();
    }
}
