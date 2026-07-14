import { useState } from 'react';
import { Navigate } from 'react-router-dom';
import Typography from '@mui/material/Typography';
import Tabs from '@mui/material/Tabs';
import Tab from '@mui/material/Tab';
import Paper from '@mui/material/Paper';
import Table from '@mui/material/Table';
import TableBody from '@mui/material/TableBody';
import TableCell from '@mui/material/TableCell';
import TableHead from '@mui/material/TableHead';
import TableRow from '@mui/material/TableRow';
import Box from '@mui/material/Box';
import Button from '@mui/material/Button';
import IconButton from '@mui/material/IconButton';
import Dialog from '@mui/material/Dialog';
import DialogTitle from '@mui/material/DialogTitle';
import DialogContent from '@mui/material/DialogContent';
import DialogActions from '@mui/material/DialogActions';
import TextField from '@mui/material/TextField';
import MenuItem from '@mui/material/MenuItem';
import Alert from '@mui/material/Alert';
import Snackbar from '@mui/material/Snackbar';
import AddIcon from '@mui/icons-material/Add';
import EditIcon from '@mui/icons-material/Edit';
import DeleteIcon from '@mui/icons-material/Delete';
import * as api from '../api/api';
import { useApi } from '../hooks/useApi';
import { useAuth } from '../auth/AuthContext';
import { Cargando, ErrorMensaje, Vacio } from '../components/Estado';

/**
 * Pantalla de administración del catálogo (solo rol ADMIN): tres pestañas con
 * el CRUD de materias, profesores y cátedras. El chequeo de rol de acá es solo
 * UX (no mostrar una pantalla que no se puede usar); la protección real está
 * en el backend, que responde 403 a las escrituras sin rol ADMIN.
 *
 * Las tres pestañas repiten el mismo patrón que el resto de la app:
 * useApi para la lista + un diálogo para crear/editar + confirmación de
 * borrado + recargar() tras cada mutación.
 */
export default function AdminPage() {
  const { usuario, cargando } = useAuth();
  const [pestania, setPestania] = useState(0);

  // Hasta no saber si hay sesión (getMe inicial) no decidimos nada: si
  // redirigiéramos ya, un admin con sesión válida rebotaría al recargar /admin.
  if (cargando) return <Cargando />;
  if (!usuario || usuario.rol !== 'ADMIN') return <Navigate to="/" replace />;

  return (
    <>
      <Typography variant="h4" gutterBottom>
        Administración
      </Typography>

      <Tabs value={pestania} onChange={(e, v) => setPestania(v)} sx={{ mb: 2 }}>
        <Tab label="Materias" />
        <Tab label="Profesores" />
        <Tab label="Cátedras" />
      </Tabs>

      {pestania === 0 && <MateriasAdmin />}
      {pestania === 1 && <ProfesoresAdmin />}
      {pestania === 2 && <CatedrasAdmin />}
    </>
  );
}

/**
 * Diálogo de confirmación de borrado, compartido por las tres pestañas.
 * `item` es null cuando está cerrado; si no, el texto que describe lo que se borra.
 */
function ConfirmarBorrado({ item, onConfirmar, onCerrar }) {
  return (
    <Dialog open={item !== null} onClose={onCerrar}>
      <DialogTitle>¿Borrar {item}?</DialogTitle>
      <DialogActions>
        <Button onClick={onCerrar}>Cancelar</Button>
        <Button color="error" onClick={onConfirmar}>
          Borrar
        </Button>
      </DialogActions>
    </Dialog>
  );
}

/** Aviso flotante para errores de borrado (ej: 409 "tiene cátedras asociadas"). */
function AvisoError({ mensaje, onCerrar }) {
  // Ojo MUI: sin ignorar 'clickaway', el mismo click que dispara la acción que
  // abre el Snackbar puede llegarle al ClickAwayListener y cerrarlo al instante.
  // Se cierra solo por timeout o por la cruz del Alert.
  function onClose(evento, razon) {
    if (razon !== 'clickaway') {
      onCerrar();
    }
  }

  return (
    <Snackbar open={!!mensaje} autoHideDuration={5000} onClose={onClose}>
      <Alert severity="error" onClose={onCerrar}>
        {mensaje}
      </Alert>
    </Snackbar>
  );
}

// -------------------- Materias --------------------

function MateriasAdmin() {
  const { data: materias, cargando, error, recargar } = useApi(api.getMaterias, []);

  // dialogo: null (cerrado) | { id: null, nombre: '' } (crear) | { id, nombre } (editar)
  const [dialogo, setDialogo] = useState(null);
  const [errorDialogo, setErrorDialogo] = useState(null);
  const [aBorrar, setABorrar] = useState(null); // materia seleccionada para borrar
  const [aviso, setAviso] = useState(null);

  async function guardar() {
    try {
      if (dialogo.id === null) {
        await api.crearMateria(dialogo.nombre);
      } else {
        await api.editarMateria(dialogo.id, dialogo.nombre);
      }
      setDialogo(null);
      setErrorDialogo(null);
      recargar();
    } catch (err) {
      // 400 (nombre vacío) o 409 (duplicada): se muestra dentro del diálogo.
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
      <Box sx={{ display: 'flex', justifyContent: 'flex-end', mb: 2 }}>
        <Button
          variant="contained"
          startIcon={<AddIcon />}
          onClick={() => setDialogo({ id: null, nombre: '' })}
        >
          Agregar materia
        </Button>
      </Box>

      {materias.length === 0 ? (
        <Vacio>No hay materias cargadas.</Vacio>
      ) : (
        <Paper>
          <Table size="small">
            <TableHead>
              <TableRow>
                <TableCell>Nombre</TableCell>
                <TableCell align="right">Acciones</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {materias.map((m) => (
                <TableRow key={m.id}>
                  <TableCell>{m.nombre}</TableCell>
                  <TableCell align="right">
                    <IconButton
                      size="small"
                      aria-label="editar"
                      onClick={() => setDialogo({ id: m.id, nombre: m.nombre })}
                    >
                      <EditIcon fontSize="small" />
                    </IconButton>
                    <IconButton
                      size="small"
                      aria-label="borrar"
                      onClick={() => setABorrar(m)}
                    >
                      <DeleteIcon fontSize="small" />
                    </IconButton>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </Paper>
      )}

      {/* Crear / editar (el mismo diálogo: cambia el título y el endpoint). */}
      <Dialog open={dialogo !== null} onClose={() => setDialogo(null)} fullWidth>
        <DialogTitle>{dialogo?.id === null ? 'Nueva materia' : 'Editar materia'}</DialogTitle>
        <DialogContent>
          {errorDialogo && (
            <Alert severity="error" sx={{ mb: 2 }}>
              {errorDialogo}
            </Alert>
          )}
          <TextField
            autoFocus
            fullWidth
            label="Nombre"
            margin="dense"
            value={dialogo?.nombre ?? ''}
            onChange={(e) => setDialogo({ ...dialogo, nombre: e.target.value })}
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDialogo(null)}>Cancelar</Button>
          <Button variant="contained" onClick={guardar}>
            Guardar
          </Button>
        </DialogActions>
      </Dialog>

      <ConfirmarBorrado
        item={aBorrar ? `la materia "${aBorrar.nombre}"` : null}
        onConfirmar={borrar}
        onCerrar={() => setABorrar(null)}
      />
      <AvisoError mensaje={aviso} onCerrar={() => setAviso(null)} />
    </>
  );
}

// -------------------- Profesores --------------------

function ProfesoresAdmin() {
  const { data: profesores, cargando, error, recargar } = useApi(api.getProfesores, []);

  const [dialogo, setDialogo] = useState(null); // { id, nombre, apellido }
  const [errorDialogo, setErrorDialogo] = useState(null);
  const [aBorrar, setABorrar] = useState(null);
  const [aviso, setAviso] = useState(null);

  async function guardar() {
    try {
      if (dialogo.id === null) {
        await api.crearProfesor(dialogo.nombre, dialogo.apellido);
      } else {
        await api.editarProfesor(dialogo.id, dialogo.nombre, dialogo.apellido);
      }
      setDialogo(null);
      setErrorDialogo(null);
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
      <Box sx={{ display: 'flex', justifyContent: 'flex-end', mb: 2 }}>
        <Button
          variant="contained"
          startIcon={<AddIcon />}
          onClick={() => setDialogo({ id: null, nombre: '', apellido: '' })}
        >
          Agregar profesor
        </Button>
      </Box>

      {profesores.length === 0 ? (
        <Vacio>No hay profesores cargados.</Vacio>
      ) : (
        <Paper>
          <Table size="small">
            <TableHead>
              <TableRow>
                <TableCell>Apellido</TableCell>
                <TableCell>Nombre</TableCell>
                <TableCell align="right">Acciones</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {profesores.map((p) => (
                <TableRow key={p.id}>
                  <TableCell>{p.apellido}</TableCell>
                  <TableCell>{p.nombre}</TableCell>
                  <TableCell align="right">
                    <IconButton
                      size="small"
                      aria-label="editar"
                      onClick={() =>
                        setDialogo({ id: p.id, nombre: p.nombre, apellido: p.apellido })
                      }
                    >
                      <EditIcon fontSize="small" />
                    </IconButton>
                    <IconButton
                      size="small"
                      aria-label="borrar"
                      onClick={() => setABorrar(p)}
                    >
                      <DeleteIcon fontSize="small" />
                    </IconButton>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </Paper>
      )}

      <Dialog open={dialogo !== null} onClose={() => setDialogo(null)} fullWidth>
        <DialogTitle>{dialogo?.id === null ? 'Nuevo profesor' : 'Editar profesor'}</DialogTitle>
        <DialogContent>
          {errorDialogo && (
            <Alert severity="error" sx={{ mb: 2 }}>
              {errorDialogo}
            </Alert>
          )}
          <TextField
            autoFocus
            fullWidth
            label="Nombre"
            margin="dense"
            value={dialogo?.nombre ?? ''}
            onChange={(e) => setDialogo({ ...dialogo, nombre: e.target.value })}
          />
          <TextField
            fullWidth
            label="Apellido"
            margin="dense"
            value={dialogo?.apellido ?? ''}
            onChange={(e) => setDialogo({ ...dialogo, apellido: e.target.value })}
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDialogo(null)}>Cancelar</Button>
          <Button variant="contained" onClick={guardar}>
            Guardar
          </Button>
        </DialogActions>
      </Dialog>

      <ConfirmarBorrado
        item={aBorrar ? `a ${aBorrar.nombre} ${aBorrar.apellido}` : null}
        onConfirmar={borrar}
        onCerrar={() => setABorrar(null)}
      />
      <AvisoError mensaje={aviso} onCerrar={() => setAviso(null)} />
    </>
  );
}

// -------------------- Cátedras --------------------

function CatedrasAdmin() {
  const { data: catedras, cargando, error, recargar } = useApi(api.getCatedras, []);
  // Para los selects del alta: la cátedra se crea eligiendo profesor y materia.
  const { data: materias } = useApi(api.getMaterias, []);
  const { data: profesores } = useApi(api.getProfesores, []);

  const [dialogo, setDialogo] = useState(null); // { profesorId: '', materiaId: '' }
  const [errorDialogo, setErrorDialogo] = useState(null);
  const [aBorrar, setABorrar] = useState(null);
  const [aviso, setAviso] = useState(null);

  async function guardar() {
    if (dialogo.profesorId === '' || dialogo.materiaId === '') {
      setErrorDialogo('Elegí un profesor y una materia');
      return;
    }
    try {
      await api.crearCatedra(dialogo.profesorId, dialogo.materiaId);
      setDialogo(null);
      setErrorDialogo(null);
      recargar();
    } catch (err) {
      // 409: ese profesor ya tiene cátedra en esa materia.
      setErrorDialogo(err.message);
    }
  }

  async function borrar() {
    try {
      await api.borrarCatedra(aBorrar.catedraId);
      recargar();
    } catch (err) {
      // 409: la cátedra tiene reviews; no se borra en cascada.
      setAviso(err.message);
    } finally {
      setABorrar(null);
    }
  }

  if (cargando) return <Cargando />;
  if (error) return <ErrorMensaje error={error} />;

  return (
    <>
      <Box sx={{ display: 'flex', justifyContent: 'flex-end', mb: 2 }}>
        <Button
          variant="contained"
          startIcon={<AddIcon />}
          onClick={() => setDialogo({ profesorId: '', materiaId: '' })}
        >
          Agregar cátedra
        </Button>
      </Box>

      {catedras.length === 0 ? (
        <Vacio>No hay cátedras cargadas.</Vacio>
      ) : (
        <Paper>
          <Table size="small">
            <TableHead>
              <TableRow>
                <TableCell>Materia</TableCell>
                <TableCell>Profesor</TableCell>
                <TableCell align="right">Acciones</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {catedras.map((c) => (
                <TableRow key={c.catedraId}>
                  <TableCell>{c.materiaNombre}</TableCell>
                  <TableCell>
                    {c.apellidoProfesor}, {c.nombreProfesor}
                  </TableCell>
                  <TableCell align="right">
                    {/* Sin editar: una cátedra ES el par profesor+materia; cambiarlo
                        es borrar esta y crear otra. */}
                    <IconButton
                      size="small"
                      aria-label="borrar"
                      onClick={() => setABorrar(c)}
                    >
                      <DeleteIcon fontSize="small" />
                    </IconButton>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </Paper>
      )}

      <Dialog open={dialogo !== null} onClose={() => setDialogo(null)} fullWidth>
        <DialogTitle>Nueva cátedra</DialogTitle>
        <DialogContent>
          {errorDialogo && (
            <Alert severity="error" sx={{ mb: 2 }}>
              {errorDialogo}
            </Alert>
          )}
          <TextField
            select
            fullWidth
            label="Profesor"
            margin="dense"
            value={dialogo?.profesorId ?? ''}
            onChange={(e) => setDialogo({ ...dialogo, profesorId: e.target.value })}
          >
            {(profesores ?? []).map((p) => (
              <MenuItem key={p.id} value={p.id}>
                {p.apellido}, {p.nombre}
              </MenuItem>
            ))}
          </TextField>
          <TextField
            select
            fullWidth
            label="Materia"
            margin="dense"
            value={dialogo?.materiaId ?? ''}
            onChange={(e) => setDialogo({ ...dialogo, materiaId: e.target.value })}
          >
            {(materias ?? []).map((m) => (
              <MenuItem key={m.id} value={m.id}>
                {m.nombre}
              </MenuItem>
            ))}
          </TextField>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDialogo(null)}>Cancelar</Button>
          <Button variant="contained" onClick={guardar}>
            Guardar
          </Button>
        </DialogActions>
      </Dialog>

      <ConfirmarBorrado
        item={
          aBorrar
            ? `la cátedra de ${aBorrar.apellidoProfesor} en ${aBorrar.materiaNombre}`
            : null
        }
        onConfirmar={borrar}
        onCerrar={() => setABorrar(null)}
      />
      <AvisoError mensaje={aviso} onCerrar={() => setAviso(null)} />
    </>
  );
}
