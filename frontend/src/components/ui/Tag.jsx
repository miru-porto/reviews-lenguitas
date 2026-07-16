/**
 * Pill de etiqueta. `variant`: neutral | accent | outline. Si se pasa `onClick`
 * se renderiza como <button> (clickeable, ej: el toggle de "útil"); si no, como
 * <span> de solo lectura.
 */
export default function Tag({ variant = 'neutral', onClick, className = '', children, ...rest }) {
  const clases = `tag tag-${variant} ${className}`.trim();
  if (onClick || rest.type) {
    return (
      <button type="button" className={clases} onClick={onClick} {...rest}>
        {children}
      </button>
    );
  }
  return (
    <span className={clases} {...rest}>
      {children}
    </span>
  );
}
