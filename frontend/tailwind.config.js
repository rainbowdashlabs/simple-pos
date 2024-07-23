/** @type {import('tailwindcss').Config} */
export default {
    darkMode: 'selector',
    content: [
        "./index.html",
        "./src/**/*.{vue,js,ts,jsx,tsx}",
    ], theme: {
        extend: {
            colors: {
                "warn-d": "#600202",
                "warn": "#ff9d9d",
                "okay-d": "#085200",
                "okay": "#b8eea1",
                "bg": "#e8e8e8",
                "bg-d": "#121417",
                "dark": "#121417",
                "primary": "#e0ecf6",
                "primary-d": "#484848",
                "primary-accent": "#6bb2fc",
                "primary-accent-d": "#365873",
                "secondary": "#bad0e7",
                "secondary-d": "#243254",
                "bright": "#f1f1f1",
                "highlight": "#6899CA",
                "accent": "#5193d5",
                "accent-d": "#2062af"
            }
        },
    },
    screens: {},
    plugins: [
        require('@tailwindcss/forms')
    ],
}

