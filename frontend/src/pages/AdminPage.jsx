import { useState } from 'react';
import { Navigate } from 'react-router-dom';
import * as api from '../api/api';
import { useApi } from '../hooks/useApi';
import { useAuth } from '../auth/AuthContext';
import { Cargando, ErrorMensaje, Vacio } from '../components/Estado';
import Button from '../components/ui/Button';
import Segmented from '../components/ui/Segmented';
import Dialog from '../components/ui/Dialog';
import Toast from '../components/ui/Toast';
import { Field, Input, Select } from '../components/ui/Field';
import { IconPlus, IconEdit, IconTrash } from '../components/ui/icons';

/**
 * Pantalla de administración del catálogo (solo rol ADMIN): tres pestañas con
 * el CRUD de materias, profesores y cátedras. El chequeo de rol de acá es solo
 * UX; la protección real está en el backend, que responde 403 a las escrituras
 * sin rol ADMIN. Cada pestaña repite el patrón useApi + diálogo + confirmación
 * de borrado + recargar() tras cada mutación.
 */
const PESTANIAS = [
  { value: 'materias', label: 'Materias' },
  { value: 'profesores', label: 'Profesores' },
  { value: 'catedras', label: 'Cátedras' },
];

export default function AdminPage() {
  const { usuario, cargando } = useAuth();
  const [pestania, setPestania] = useState('materias');

  // Hasta no saber si hay sesión (getMe inicial) no decidimos nada.
  if (cargando) return <Cargando />;
  if (!usuario || usuario.rol !== 'ADMIN') return <Navigate to="/" replace />;

  return (
    <>
      <h2>Administración</h2>
      <div style={{ margin: 'var(--space-4) 0 var(--space-6)' }}>
        <Segmented opciones={PESTANIAS} value={pestania} onChange={setPestania} />
      </div>

      {pestania === 'materias' && <MateriasAdmin />}
      {pestania === 'profesores' && <ProfesoresAdmin />}
      {pestania === 'catedras' && <CatedrasAdmin />}
    </>
  );
}

/** Diálogo de confirmación de borrado, compartido por las tres pestañas. */
function ConfirmarBorrado({ item, onConfirmar, onCerrar }) {
  if (!item) return null;
  return (
    <Dialog onClose={onCerrar} labelledBy="borrar-title">
      <div className="dialog-title" id="borrar-title">¿Borrar {item}?</div>
      <div className="dialog-actions">
        <Button variant="secondary" onClick={onCerrar}>Cancelar</Button>
        <Button variant="danger" onClick={onConfirmar}>Borrar</Button>
      </div>
    </Dialog>
  );
}

/** Barra con el botón de "agregar" alineado a la derecha. */
function BarraAgregar({ children, onClick }) {
  return (
    <div style={{ display: 'flex', justifyContent: 'flex-end', marginBottom: 'var(--space-4)' }}>
      <Button variant="primary" icon={IconPlus} onClick={onClick}>{children}</Button>
    </div>
  );
}

/** Botones de acción (editar/borrar) de una fila. */
function AccionesFila({ onEditar, onBorrar }) {
  return (
    <span style={{ display: 'inline-flex', gap: 4, justifyContent: 'flex-end' }}>
      {onEditar && (
        <Button variant="ghost" className="btn-icon" aria-label="editar" onClick={onEditar}>
          <IconEdit size={16} />
        </Button>
      )}
      <Button variant="ghost" className="btn-icon" aria-label="borrar" onClick={onBorrar} style={{ color: '#e5484d' }}>
        <IconTrash size={16} />
      </Button>
    </span>
  );
}

// -------------------- Materias --------------------

function MateriasAdmin() {
  const { data: materias, cargando, error, recargar } = useApi(api.getMaterias, []);

  // dialogo: null | { id:null, nombre:'', anio:'' } (crear) | { id, nombre, anio } (editar)
  const [dialogo, setDialogo] = useState(null);
  const [errorDialogo, setErrorDialogo] = useState('');
  const [aBorrar, setABorrar] = useState(null);
  const [aviso, setAviso] = useState('');

  async function guardar(e) {
    e.preventDefault();
    if (dialogo.anio === '') return setErrorDialogo('Elegí el año de cursada');
    try {
      if (dialogo.id === null) await api.crearMateria(dialogo.nombre, dialogo.anio);
      else await api.editarMateria(dialogo.id, dialogo.nombre, dialogo.anio);
      setDialogo(null);
      setErrorDialogo('');
      recargar();
    } catch (err) {
      setErrorDialogo(err.message);
    }
  }

  async function borrar() {
    try {
      await api.borrarMateria(aBorrar.id);
      recargar();
    } catch (err) {
      setAviso(err.message);
    } finally {
      setABorrar(null);
    }
  }

  if (cargando) return <Cargando />;
  if (error) return <ErrorMensaje error={error} />;

  return (
    <>
      <BarraAgregar onClick={() => setDialogo({ id: null, nombre: '', anio: '' })}>Agregar materia</BarraAgregar>

      {materias.length === 0 ? (
        <Vacio>No hay materias cargadas.</Vacio>
      ) : (
        <div className="card elev-sm" style={{ padding: 0, overflowX: 'auto' }}>
          <table className="table">
            <thead>
              <tr><th>Nombre</th><th>Año</th><th>Acciones</th></tr>
            </thead>
            <tbody>
              {materias.map((m) => (
                <tr key={m.id}>
                  <td>{m.nombre}</td>
                  <td>{m.anio ?? '—'}</td>
                  <td>
                    <AccionesFila
                      onEditar={() => setDialogo({ id: m.id, nombre: m.nombre, anio: m.anio ?? '' })}
                      onBorrar={() => setABorrar(m)}
                    />
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {dialogo && (
        <Dialog onClose={() => setDialogo(null)} labelledBy="mat-title">
          <form onSubmit={guardar} style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-4)' }}>
            <div className="dialog-title" id="mat-title">{dialogo.id === null ? 'Nueva materia' : 'Editar materia'}</div>
            {errorDialogo && <div className="alert alert-error">{errorDialogo}</div>}
            <Field label="Nombre" htmlFor="mat-nombre">
              <Input id="mat-nombre" autoFocus value={dialogo.nombre} onChange={(e) => setDialogo({ ...dialogo, nombre: e.target.value })} />
            </Field>
            <Field label="Año de cursada" htmlFor="mat-anio">
              <Select
                id="mat-anio"
                value={dialogo.anio}
                onChange={(e) => setDialogo({ ...dialogo, anio: e.target.value === '' ? '' : Number(e.target.value) })}
                placeholder="Elegí el año"
              >
                {[1, 2, 3, 4, 5].map((a) => (
                  <option key={a} value={a}>{a}°{a === 5 ? ' (plan de 5 años)' : ''}</option>
                ))}
              </Select>
            </Field>
            <div className="dialog-actions">
              <Button variant="secondary" onClick={() => setDialogo(null)}>Cancelar</Button>
              <Button variant="primary" type="submit">Guardar</Button>
            </div>
          </form>
        </Dialog>
      )}

      <ConfirmarBorrado item={aBorrar ? `la materia "${aBorrar.nombre}"` : null} onConfirmar={borrar} onCerrar={() => setABorrar(null)} />
      <Toast mensaje={aviso} onCerrar={() => setAviso('')} />
    </>
  );
}

// -------------------- Profesores --------------------

function ProfesoresAdmin() {
  const { data: profesores, cargando, error, recargar } = useApi(api.getProfesores, []);

  const [dialogo, setDialogo] = useState(null); // { id, nombre, apellido }
  const [errorDialogo, setErrorDialogo] = useState('');
  const [aBorrar, setABorrar] = useState(null);
  const [aviso, setAviso] = useState('');

  async function guardar(e) {
    e.preventDefault();
    try {
      if (dialogo.id === null) await api.crearProfesor(dialogo.nombre, dialogo.apellido);
      else await api.editarProfesor(dialogo.id, dialogo.nombre, dialogo.apellido);
      setDialogo(null);
      setErrorDialogo('');
      recargar();
    } catch (err) {
      setErrorDialogo(err.message);
    }
  }

  async function borrar() {
    try {
      await api.borrarProfesor(aBorrar.id);
      recargar();
    } catch (err) {
      setAviso(err.message);
    } finally {
      setABorrar(null);
    }
  }

  if (cargando) return <Cargando />;
  if (error) return <ErrorMensaje error={error} />;

  return (
    <>
      <BarraAgregar onClick={() => setDialogo({ id: null, nombre: '', apellido: '' })}>Agregar profesor</BarraAgregar>

      {profesores.length === 0 ? (
        <Vacio>No hay profesores cargados.</Vacio>
      ) : (
        <div className="card elev-sm" style={{ padding: 0, overflowX: 'auto' }}>
          <table className="table">
            <thead>
              <tr><th>Apellido</th><th>Nombre</th><th>Acciones</th></tr>
            </thead>
            <tbody>
              {profesores.map((p) => (
                <tr key={p.id}>
                  <td>{p.apellido}</td>
                  <td>{p.nombre || '—'}</td>
                  <td>
                    <AccionesFila
                      onEditar={() => setDialogo({ id: p.id, nombre: p.nombre, apellido: p.apellido })}
                      onBorrar={() => setABorrar(p)}
                    />
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {dialogo && (
        <Dialog onClose={() => setDialogo(null)} labelledBy="prof-title">
          <form onSubmit={guardar} style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-4)' }}>
            <div className="dialog-title" id="prof-title">{dialogo.id === null ? 'Nuevo profesor' : 'Editar profesor'}</div>
            {errorDialogo && <div className="alert alert-error">{errorDialogo}</div>}
            <Field label="Apellido" htmlFor="prof-apellido">
              <Input id="prof-apellido" autoFocus value={dialogo.apellido} onChange={(e) => setDialogo({ ...dialogo, apellido: e.target.value })} />
            </Field>
            {/* Los horarios solo publican apellidos: el nombre puede no saberse. */}
            <Field label="Nombre (opcional)" htmlFor="prof-nombre">
              <Input id="prof-nombre" value={dialogo.nombre} onChange={(e) => setDialogo({ ...dialogo, nombre: e.target.value })} />
            </Field>
            <div className="dialog-actions">
              <Button variant="secondary" onClick={() => setDialogo(null)}>Cancelar</Button>
              <Button variant="primary" type="submit">Guardar</Button>
            </div>
          </form>
        </Dialog>
      )}

      <ConfirmarBorrado item={aBorrar ? `a ${`${aBorrar.nombre} ${aBorrar.apellido}`.trim()}` : null} onConfirmar={borrar} onCerrar={() => setABorrar(null)} />
      <Toast mensaje={aviso} onCerrar={() => setAviso('')} />
    </>
  );
}

// -------------------- Cátedras --------------------

function CatedrasAdmin() {
  const { data: catedras, cargando, error, recargar } = useApi(api.getCatedras, []);
  const { data: materias } = useApi(api.getMaterias, []);
  const { data: profesores } = useApi(api.getProfesores, []);

  const [dialogo, setDialogo] = useState(null); // { profesorId:'', materiaId:'' }
  const [errorDialogo, setErrorDialogo] = useState('');
  const [aBorrar, setABorrar] = useState(null);
  const [aviso, setAviso] = useState('');

  async function guardar(e) {
    e.preventDefault();
    if (dialogo.profesorId === '' || dialogo.materiaId === '') return setErrorDialogo('Elegí un profesor y una materia');
    try {
      await api.crearCatedra(dialogo.profesorId, dialogo.materiaId);
      setDialogo(null);
      setErrorDialogo('');
      recargar();
    } catch (err) {
      setErrorDialogo(err.message);
    }
  }

  async function borrar() {
    try {
      await api.borrarCatedra(aBorrar.catedraId);
      recargar();
    } catch (err) {
      setAviso(err.message);
    } finally {
      setABorrar(null);
    }
  }

  if (cargando) return <Cargando />;
  if (error) return <ErrorMensaje error={error} />;

  return (
    <>
      <BarraAgregar onClick={() => setDialogo({ profesorId: '', materiaId: '' })}>Agregar cátedra</BarraAgregar>

      {catedras.length === 0 ? (
        <Vacio>No hay cátedras cargadas.</Vacio>
      ) : (
        <div className="card elev-sm" style={{ padding: 0, overflowX: 'auto' }}>
          <table className="table">
            <thead>
              <tr><th>Materia</th><th>Profesor</th><th>Acciones</th></tr>
            </thead>
            <tbody>
              {catedras.map((c) => (
                <tr key={c.catedraId}>
                  <td>{c.materiaNombre}</td>
                  <td>{c.nombreProfesor ? `${c.apellidoProfesor}, ${c.nombreProfesor}` : c.apellidoProfesor}</td>
                  <td>
                    {/* Sin editar: una cátedra ES el par profesor+materia. */}
                    <AccionesFila onBorrar={() => setABorrar(c)} />
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {dialogo && (
        <Dialog onClose={() => setDialogo(null)} labelledBy="cat-title">
          <form onSubmit={guardar} style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-4)' }}>
            <div className="dialog-title" id="cat-title">Nueva cátedra</div>
            {errorDialogo && <div className="alert alert-error">{errorDialogo}</div>}
            <Field label="Profesor" htmlFor="cat-profe">
              <Select
                id="cat-profe"
                value={dialogo.profesorId}
                onChange={(e) => setDialogo({ ...dialogo, profesorId: e.target.value === '' ? '' : Number(e.target.value) })}
                placeholder="Elegí un profesor"
              >
                {(profesores ?? []).map((p) => (
                  <option key={p.id} value={p.id}>{p.nombre ? `${p.apellido}, ${p.nombre}` : p.apellido}</option>
                ))}
              </Select>
            </Field>
            <Field label="Materia" htmlFor="cat-materia">
              <Select
                id="cat-materia"
                value={dialogo.materiaId}
                onChange={(e) => setDialogo({ ...dialogo, materiaId: e.target.value === '' ? '' : Number(e.target.value) })}
                placeholder="Elegí una materia"
              >
                {(materias ?? []).map((m) => (
                  <option key={m.id} value={m.id}>{m.nombre}</option>
                ))}
              </Select>
            </Field>
            <div className="dialog-actions">
              <Button variant="secondary" onClick={() => setDialogo(null)}>Cancelar</Button>
              <Button variant="primary" type="submit">Guardar</Button>
            </div>
          </form>
        </Dialog>
      )}

      <ConfirmarBorrado
        item={aBorrar ? `la cátedra de ${aBorrar.apellidoProfesor} en ${aBorrar.materiaNombre}` : null}
        onConfirmar={borrar}
        onCerrar={() => setABorrar(null)}
      />
      <Toast mensaje={aviso} onCerrar={() => setAviso('')} />
    </>
  );
}
