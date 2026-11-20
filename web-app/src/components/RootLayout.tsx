import { Outlet, useLocation } from 'react-router-dom';
import { MusicPlayer } from './MusicPlayer';
import { ImportStatus } from './ImportStatus';
import { useMusic } from '../context/musicContext';

const COMPACT_ROUTES = ['/notes/editor', '/latex/editor', '/notes/copy-to-latex'];

export const RootLayout = () => {
  const { pathname } = useLocation();
  const { queue, hidden } = useMusic();
  const compact = COMPACT_ROUTES.some((route) => pathname.startsWith(route));
  const showFullBar = queue.length > 0 && !compact && !hidden;

  return (
    <>
      <div className={showFullBar ? 'pb-56 sm:pb-28' : ''}>
        <Outlet />
      </div>
      <ImportStatus />
      <MusicPlayer compact={compact} />
    </>
  );
};
