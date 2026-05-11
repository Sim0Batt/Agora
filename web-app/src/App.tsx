import { HomePage } from './pages/HomePage';
import { createBrowserRouter, RouterProvider } from 'react-router-dom';
import { LatexNotes } from './pages/LatexNotes';
import LatexEditor from './pages/LatexEditor';
import { NotesPage } from './pages/NotesPage';
import NoteEditor from './pages/NoteEditor';

const router = createBrowserRouter([
  {
    path: '/',
    element: <HomePage />,
  },
  {
    path: '/latex',
    element: <LatexNotes />,
  },
  {
    path: '/latex/editor/:id',
    element: <LatexEditor />,
  },
  {
    path: '/notes',
    element: <NotesPage />,
  },
  {
    path: '/notes/editor/:id',
    element: <NoteEditor />,
  },
]);

function App() {
  return <RouterProvider router={router} />;
}

export default App;
