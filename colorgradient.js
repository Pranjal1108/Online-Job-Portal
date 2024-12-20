// Define the specific colors you want to use for the gradient
const gradientColors = [
    '#2c2885',  // Dark Purple
    '#454cf6',  // Light Purple
    '#7390f5',  // Lavender Blue
    '#a3b4ff',  // Light Blue
    '#9591e6',  // Soft Purple
    '#595685',  // Medium Purple
    '#4b3f74',  // Darker Purple
    '#3c2a60',  // Deep Purple
    '#1f1a3d'   // Very Dark Purple
];

// Function to generate random gradient direction
function getRandomDirection() {
    const randomDegree = Math.floor(Math.random() * 360); // Random angle between 0 and 360 degrees
    return `${randomDegree}deg`;
}

// Function to generate random gradient colors
function getRandomGradient() {
    // Pick 4 random colors from the gradientColors array
    const color1 = gradientColors[Math.floor(Math.random() * gradientColors.length)];
    const color2 = gradientColors[Math.floor(Math.random() * gradientColors.length)];
    const color3 = gradientColors[Math.floor(Math.random() * gradientColors.length)];
    const color4 = gradientColors[Math.floor(Math.random() * gradientColors.length)];

    return `linear-gradient(${getRandomDirection()}, ${color1}, ${color2}, ${color3}, ${color4})`;
}

// Function to apply a random gradient with random movement to the body background
function applyRandomGradient() {
    // Get a random gradient
    const gradient = getRandomGradient();

    // Apply the random gradient as the background
    document.body.style.background = gradient;

    // Set the background size and random movement direction (animation)
    document.body.style.backgroundSize = '300% 300%';
    document.body.style.animation = 'randomGradientFlow 10s ease infinite';
}

// Call the function to apply the random gradient effect initially
applyRandomGradient();

// Add the glitter effect
function addGlitterEffect() {
    const glitterContainer = document.createElement('div');
    glitterContainer.classList.add('glitter');
    document.body.appendChild(glitterContainer);
}

// Call the function to add the glitter effect
addGlitterEffect();
