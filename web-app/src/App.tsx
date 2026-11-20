import { HomePage } from './pages/HomePage';
import { createBrowserRouter, RouterProvider } from 'react-router-dom';
import { LatexNotes } from './pages/LatexNotes';
import LatexEditor from './pages/LatexEditor';
import { NotesPage } from './pages/NotesPage';
import NoteEditor from './pages/NoteEditor';
import CopyNoteToLatex from './pages/CopyNoteToLatex';
import { MusicPage } from './pages/MusicPage';
import { PlaylistsPage } from './pages/PlaylistsPage';
import { PlaylistDetailPage } from './pages/PlaylistDetailPage';
import { SettingsPage } from './pages/SettingsPage';
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
      {
        path: '/music/playlists',
        element: <PlaylistsPage />,
      },
      {
        path: '/music/playlists/:id',
        element: <PlaylistDetailPage />,
      },
      {
        path: '/dashboard/user',
        element: <SettingsPage />,
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
