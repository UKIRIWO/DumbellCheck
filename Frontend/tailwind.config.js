/** @type {import('tailwindcss').Config} */
module.exports = {
  content: ['./src/**/*.{html,ts,scss}'],
  theme: {
    extend: {
      keyframes: {
        float1: {
          '0%, 100%': { transform: 'translate(0, 0) scale(1)' },
          '50%': { transform: 'translate(-30px, 30px) scale(1.05)' },
        },
        float2: {
          '0%, 100%': { transform: 'translate(0, 0) scale(1)' },
          '50%': { transform: 'translate(20px, -20px) scale(1.08)' },
        },
      },
      animation: {
        float1: 'float1 8s ease-in-out infinite',
        float2: 'float2 10s ease-in-out infinite',
      },
      colors: {
        bg: 'var(--color-bg)',
        navy: 'var(--color-navy)',
        blue: 'var(--color-blue)',
        blueDark: 'var(--color-blue-dark)',
        sky: 'var(--color-sky)',
        silver: 'var(--color-silver)',
        surface: 'var(--color-surface)',
        danger: 'var(--color-danger)',
        success: 'var(--color-success)',
        warning: 'var(--color-warning)',
        muted: 'var(--color-muted)',
        border: 'var(--color-border)',
        borderSoft: 'var(--color-border-soft)',
        footer: 'var(--color-footer)',
        ice: 'var(--color-ice)',
        charcoal: 'var(--color-charcoal)',
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
