/** @type {import('tailwindcss').Config} */
module.exports = {
  content: ['./src/**/*.{html,ts}'],
  theme: {
    extend: {
      colors: {
        primary: {
          light: 'var(--color-primary-light)',
          dark: 'var(--color-primary-dark)',
        },
        brand: {
          text: 'var(--color-text-main)',
          blue: 'var(--color-secondary-blue)',
          gray: 'var(--color-secondary-gray)',
        },
      },
      fontFamily: {
        heading: ['Montserrat', 'sans-serif'],
        body: ['Roboto', 'sans-serif'],
      },
    },
  },
  plugins: [],
};
