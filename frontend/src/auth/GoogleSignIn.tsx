import { useEffect, useRef } from 'react';

interface Props {
  clientId: string;
  onCredential: (credential: string) => void;
}

/** Renders the official Google Sign-In button once the GSI script is ready. */
export function GoogleSignIn({ clientId, onCredential }: Props) {
  const ref = useRef<HTMLDivElement>(null);

  useEffect(() => {
    let cancelled = false;

    function tryRender() {
      if (cancelled) return;
      if (window.google && ref.current) {
        window.google.accounts.id.initialize({
          client_id: clientId,
          callback: (res) => onCredential(res.credential),
        });
        window.google.accounts.id.renderButton(ref.current, {
          theme: 'outline',
          size: 'large',
          text: 'signin_with',
        });
      } else {
        setTimeout(tryRender, 200);
      }
    }
    tryRender();

    return () => {
      cancelled = true;
    };
  }, [clientId, onCredential]);

  return <div ref={ref} />;
}
