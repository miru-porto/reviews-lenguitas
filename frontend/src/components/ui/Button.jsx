/**
 * Botón Nocturne. `variant`: primary | secondary | ghost | danger.
 * `as` permite renderizar como otro elemento (ej: el Link de react-router):
 *   <Button as={RouterLink} to="/x" variant="ghost">…</Button>
 * `icon` es un componente de ícono opcional que va antes del texto; `block`
 * lo hace de ancho completo.
 */
export default function Button({
  as: Comp = 'button',
  variant = 'secondary',
  icon: Icon,
  block = false,
  className = '',
  children,
  ...rest
}) {
  const clases = [
    'btn',
    `btn-${variant}`,
    block ? 'btn-block' : '',
    className,
  ]
    .filter(Boolean)
    .join(' ');

  // Un <button> sin type explícito envía el form que lo contiene; default a
  // "button" salvo que se pida otra cosa.
  const typeProp = Comp === 'button' && rest.type === undefined ? { type: 'button' } : {};

  return (
    <Comp className={clases} {...typeProp} {...rest}>
      {Icon && <Icon size={16} />}
      {children}
    </Comp>
  );
}
