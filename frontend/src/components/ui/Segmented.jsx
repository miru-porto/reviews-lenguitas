/**
 * Control segmentado (toggle exclusivo). `opciones` es [{ value, label }];
 * `value` es el seleccionado y `onChange(value)` se dispara al elegir otro.
 * Reemplaza al ToggleButtonGroup de MUI para ordenar reviews/cátedras.
 */
export default function Segmented({ opciones, value, onChange }) {
  return (
    <div className="seg" role="group">
      {opciones.map((o) => (
        <button
          key={o.value}
          type="button"
          className="seg-opt"
          aria-pressed={value === o.value}
          onClick={() => onChange(o.value)}
        >
          {o.label}
        </button>
      ))}
    </div>
  );
}
