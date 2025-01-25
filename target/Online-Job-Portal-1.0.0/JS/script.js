// Snowflakes Function
function createSnowflakes() {
    const snowflakesContainer = document.querySelector('.snowflakes');
    const snowflakeCount = 100;  // Number of snowflakes

    for (let i = 0; i < snowflakeCount; i++) {
        const snowflake = document.createElement('div');
        snowflake.classList.add('snowflake');
        snowflake.innerHTML = '❄';  // Snowflake character
        snowflake.style.left = `${Math.random() * 100}vw`;  // Random position
        snowflake.style.animationDuration = `${Math.random() * 5 + 5}s`;  // Random fall duration
        snowflake.style.animationDelay = `${Math.random() * 5}s`;  // Random delay
        snowflakesContainer.appendChild(snowflake);
    }
}

// Glitter Effect
function createGlitter() {
    const glitterElement = document.querySelector('.glitter');
    glitterElement.style.animationDuration = '0.5s';
}

// Job Listings Data
const jobListings = [
    {
        title: 'Software Engineer',
        description: 'Build and maintain web applications.',
        link: '#'
    },
    {
        title: 'Data Scientist',
        description: 'Analyze large datasets to extract insights.',
        link: '#'
    },
    {
        title: 'Product Manager',
        description: 'Lead product development from concept to launch.',
        link: '#'
    }
];

// Render Job Listings
function renderJobListings() {
    const jobListingsContainer = document.querySelector('.job-listings');
    jobListings.forEach(job => {
        const jobElement = document.createElement('div');
        jobElement.classList.add('job-listing');

        jobElement.innerHTML = `
            <h3>${job.title}</h3>
            <p>${job.description}</p>
            <a href="${job.link}" target="_blank">Apply Now</a>
        `;

        jobListingsContainer.appendChild(jobElement);
    });
}

// Initialize
createSnowflakes();
createGlitter();
renderJobListings();
