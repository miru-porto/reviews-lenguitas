/**
 * Campos de formulario Nocturne. `Field` es el envoltorio con label; `Input`,
 * `Textarea` y `Select` aplican la clase .input y reenvían el resto de props
 * (value, onChange, placeholder, etc.).
 */

export function Field({ label, htmlFor, children }) {
  return (
    <div className="field">
      {label && <label htmlFor={htmlFor}>{label}</label>}
      {children}
    </div>
  );
}

export function Input({ className = '', ...rest }) {
  return <input className={`input ${className}`.trim()} {...rest} />;
}

export function Textarea({ className = '', ...rest }) {
  return <textarea className={`input ${className}`.trim()} {...rest} />;
}

/**
 * Select nativo estilado. `placeholder` agrega una opción deshabilitada inicial
 * (value ""), útil cuando todavía no se eligió nada.
 */
export function Select({ className = '', placeholder, children, ...rest }) {
  return (
    <select className={`input ${className}`.trim()} {...rest}>
      {placeholder && (
        <option value="" disabled>
          {placeholder}
        </option>
      )}
      {children}
    </select>
  );
}
