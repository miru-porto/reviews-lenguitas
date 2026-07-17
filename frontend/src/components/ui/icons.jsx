/**
 * Íconos SVG inline (reemplazan @mui/icons-material). Todos comparten el mismo
 * trazo de 24x24 con currentColor, así heredan el color del texto donde se usan.
 * Props: size (px) y el resto se pasa al <svg> (className, etc.).
 */
function Svg({ size = 18, children, ...rest }) {
  return (
    <svg
      width={size}
      height={size}
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="2"
      strokeLinecap="round"
      strokeLinejoin="round"
      aria-hidden="true"
      {...rest}
    >
      {children}
    </svg>
  );
}

export const IconSearch = (p) => (
  <Svg {...p}>
    <circle cx="11" cy="11" r="7" />
    <line x1="21" y1="21" x2="16.65" y2="16.65" />
  </Svg>
);

export const IconSun = (p) => (
  <Svg {...p}>
    <circle cx="12" cy="12" r="4" />
    <line x1="12" y1="2" x2="12" y2="5" />
    <line x1="12" y1="19" x2="12" y2="22" />
    <line x1="4.2" y1="4.2" x2="6.3" y2="6.3" />
    <line x1="17.7" y1="17.7" x2="19.8" y2="19.8" />
    <line x1="2" y1="12" x2="5" y2="12" />
    <line x1="19" y1="12" x2="22" y2="12" />
    <line x1="4.2" y1="19.8" x2="6.3" y2="17.7" />
    <line x1="17.7" y1="6.3" x2="19.8" y2="4.2" />
  </Svg>
);

export const IconMoon = (p) => (
  <Svg {...p}>
    <path d="M21 12.8A8.5 8.5 0 1 1 11.2 3a6.5 6.5 0 0 0 9.8 9.8Z" />
  </Svg>
);

export const IconArrowLeft = (p) => (
  <Svg {...p}>
    <line x1="19" y1="12" x2="5" y2="12" />
    <polyline points="12 19 5 12 12 5" />
  </Svg>
);

export const IconChevronRight = (p) => (
  <Svg {...p}>
    <polyline points="9 18 15 12 9 6" />
  </Svg>
);

export const IconThumbUp = (p) => (
  <Svg {...p}>
    <path d="M7 11v9" />
    <path d="M4 22h11a2 2 0 0 0 2-1.5l1.9-6A2 2 0 0 0 17 12h-5V6a2 2 0 0 0-4 0c0 1.7-1 3.3-3 5v9Z" />
  </Svg>
);

export const IconEdit = (p) => (
  <Svg {...p}>
    <path d="M12 20h9" />
    <path d="M16.5 3.5a2.1 2.1 0 0 1 3 3L7 19l-4 1 1-4Z" />
  </Svg>
);

export const IconTrash = (p) => (
  <Svg {...p}>
    <polyline points="3 6 5 6 21 6" />
    <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2" />
  </Svg>
);

export const IconPlus = (p) => (
  <Svg {...p}>
    <line x1="12" y1="5" x2="12" y2="19" />
    <line x1="5" y1="12" x2="19" y2="12" />
  </Svg>
);

export const IconClose = (p) => (
  <Svg {...p}>
    <line x1="18" y1="6" x2="6" y2="18" />
    <line x1="6" y1="6" x2="18" y2="18" />
  </Svg>
);

export const IconMenu = (p) => (
  <Svg {...p}>
    <line x1="4" y1="7" x2="20" y2="7" />
    <line x1="4" y1="12" x2="20" y2="12" />
    <line x1="4" y1="17" x2="20" y2="17" />
  </Svg>
);

export const IconCoffee = (p) => (
  <Svg {...p}>
    <path d="M4 8h13v5a5 5 0 0 1-5 5H9a5 5 0 0 1-5-5V8Z" />
    <path d="M17 9h2a3 3 0 0 1 0 6h-2" />
    <line x1="7" y1="3" x2="7" y2="5" />
    <line x1="11" y1="3" x2="11" y2="5" />
  </Svg>
);

/**
 * La "G" de Google. Única excepción al Svg de arriba: es de relleno y con los
 * colores fijos de la marca, no hereda currentColor. Google pide su logo tal
 * cual en los botones de "Ingresar con Google", así que no se tiñe con el tema.
 */
export const IconGoogle = ({ size = 18, ...rest }) => (
  <svg width={size} height={size} viewBox="0 0 48 48" aria-hidden="true" {...rest}>
    <path
      fill="#4285F4"
      d="M45.12 24.5c0-1.56-.14-3.06-.4-4.5H24v8.51h11.84c-.51 2.75-2.06 5.08-4.39 6.64v5.52h7.11c4.16-3.83 6.56-9.47 6.56-16.17z"
    />
    <path
      fill="#34A853"
      d="M24 46c5.94 0 10.92-1.97 14.56-5.33l-7.11-5.52c-1.97 1.32-4.49 2.1-7.45 2.1-5.73 0-10.58-3.87-12.31-9.07H4.34v5.7C7.96 41.07 15.4 46 24 46z"
    />
    <path
      fill="#FBBC05"
      d="M11.69 28.18c-.44-1.32-.69-2.73-.69-4.18s.25-2.86.69-4.18v-5.7H4.34C2.85 17.09 2 20.45 2 24s.85 6.91 2.34 9.88l7.35-5.7z"
    />
    <path
      fill="#EA4335"
      d="M24 10.75c3.23 0 6.13 1.11 8.41 3.29l6.31-6.31C34.91 4.18 29.93 2 24 2 15.4 2 7.96 6.93 4.34 14.12l7.35 5.7c1.73-5.2 6.58-9.07 12.31-9.07z"
    />
  </svg>
);
