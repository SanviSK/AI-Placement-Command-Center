/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  darkMode: 'class',
  theme: {
    extend: {
      colors: {
        background: '#0B0F19',
        card: '#161C2C',
        border: '#2A344A',
        primary: {
          DEFAULT: '#6366F1', // Indigo
          hover: '#4F46E5',
          light: '#818CF8'
        },
        secondary: {
          DEFAULT: '#EC4899', // Pink
          hover: '#DB2777',
          light: '#F472B6'
        },
        success: {
          DEFAULT: '#10B981', // Emerald
          hover: '#059669',
          light: '#34D399'
        },
        warning: {
          DEFAULT: '#F59E0B', // Amber
          hover: '#D97706',
          light: '#FBBF24'
        },
        info: {
          DEFAULT: '#3B82F6', // Blue
          hover: '#2563EB',
          light: '#60A5FA'
        },
        dark: {
          100: '#1F293D',
          200: '#161C2C',
          300: '#0F1524',
          900: '#0B0F19'
        }
      },
      fontFamily: {
        sans: ['Inter', 'sans-serif'],
        display: ['Outfit', 'sans-serif'],
      },
      boxShadow: {
        'glow-primary': '0 0 15px rgba(99, 102, 241, 0.4)',
        'glow-secondary': '0 0 15px rgba(236, 72, 153, 0.4)',
        'glass': '0 8px 32px 0 rgba(0, 0, 0, 0.37)'
      },
      backgroundImage: {
        'gradient-dark': 'linear-gradient(135deg, #0F1524 0%, #0B0F19 100%)',
        'gradient-card': 'linear-gradient(180deg, rgba(22, 28, 44, 0.8) 0%, rgba(15, 21, 36, 0.8) 100%)',
        'gradient-accent': 'linear-gradient(135deg, #6366F1 0%, #EC4899 100%)'
      }
    },
  },
  plugins: [],
}
