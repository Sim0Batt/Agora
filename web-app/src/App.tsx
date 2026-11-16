import { HomePage } from './pages/HomePage';
import { createBrowserRouter, RouterProvider } from 'react-router-dom';
import { LatexNotes } from './pages/LatexNotes';
import LatexEditor from './pages/LatexEditor';
import { NotesPage } from './pages/NotesPage';
import NoteEditor from './pages/NoteEditor';
import CopyNoteToLatex from './pages/CopyNoteToLatex';
import { MusicPage } from './pages/MusicPage';
import { RootLayout } from './components/RootLayout';
import { MusicProvider } from './context/MusicProvider';

const router = createBrowserRouter([
  {
    element: <RootLayout />,
    children: [
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
      {
        path: '/notes/copy-to-latex',
        element: <CopyNoteToLatex />,
      },
      {
        path: '/music',
        element: <MusicPage />,
      },
    ],
  },
]);

function App() {
  return (
    <MusicProvider>
      <RouterProvider router={router} />
    </MusicProvider>
  );
}

export default App;
