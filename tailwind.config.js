/** @type {import('tailwindcss').Config} */
export default {
    content: [
        "./index.html",
        "./src/**/*.{vue,js,ts,jsx,tsx}",
    ], theme: {
        extend: {
            colors: {
                "primary": "#23282D",
                "secondary": "#D1DAE3",
                "bright": "#f1f1f1",
                "highlight": "#6899CA",
                "accent": "#FF9633"
            }
        },
    },
    plugins: [],
}

