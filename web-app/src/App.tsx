import { HomePage } from './pages/HomePage';
import { createBrowserRouter, RouterProvider } from 'react-router-dom';
import { LatexNotes } from './pages/LatexNotes';
import LatexEditor from './pages/LatexEditor';

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
]);

function App() {
  return <RouterProvider router={router} />;
}

export default App;
