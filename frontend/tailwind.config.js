/** @type {import('tailwindcss').Config} */
export default {
    content: [
        "./index.html",
        "./src/**/*.{vue,js,ts,jsx,tsx}",
    ], theme: {
        extend: {
            colors: {
                "dark": "#121417",
                "primary": "#23282D",
                "primary-accent": "#434d57",
                "secondary": "#D1DAE3",
                "bright": "#f1f1f1",
                "highlight": "#6899CA",
                "accent": "#FF9633"
            }
        },
    },
    plugins: [
        require('@tailwindcss/forms')
    ],
}

