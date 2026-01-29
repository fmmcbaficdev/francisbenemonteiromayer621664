/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        // Cores da Polícia Civil MT
        primary: {
          50: '#f0f4f8',
          100: '#d9e2ec',
          200: '#bcccdc',
          300: '#9fb3c8',
          400: '#829ab1',
          500: '#627d98',
          600: '#1e3a5f',  // Azul escuro principal
          700: '#19324d',
          800: '#142a3f',
          900: '#0f2132',
        },
        gold: {
          50: '#fdf9e7',
          100: '#faf0c4',
          200: '#f5e08a',
          300: '#e9c85a',
          400: '#d4af37',  // Dourado principal
          500: '#b8952f',
          600: '#967827',
          700: '#745c1f',
          800: '#5a4718',
          900: '#463712',
        },
        accent: {
          50: '#fde8eb',
          100: '#f9c5cc',
          200: '#f49aa6',
          300: '#e86a7a',
          400: '#d94452',
          500: '#c41e3a',  // Vermelho do brasão
          600: '#a31830',
          700: '#821326',
          800: '#620e1c',
          900: '#420912',
        },
      },
    },
  },
  plugins: [],
}