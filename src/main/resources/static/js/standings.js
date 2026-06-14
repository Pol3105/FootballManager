const { useState, useEffect } = React;
const e = React.createElement;

function cssVar(name, fallback) {
    try {
        const v = getComputedStyle(document.documentElement).getPropertyValue(name).trim();
        return v || fallback;
    } catch (_) { return fallback; }
}

function StandingsTable({ tournamentId }) {
    const [rows, setRows]       = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError]     = useState(null);

    useEffect(() => {
        fetch('/api/tournament/' + tournamentId + '/standings')
            .then(r => {
                if (!r.ok) throw new Error("Error al cargar la clasificación");
                return r.json();
            })
            .then(data => { setRows(data); setLoading(false); })
            .catch(err => { setError(err.message); setLoading(false); });
    }, [tournamentId]);

    const muted    = cssVar('--muted-foreground', '#646464');
    const fg       = cssVar('--foreground', '#202020');
    const border   = cssVar('--border', '#d8d8d8');
    const card     = cssVar('--card', '#fcfcfc');
    const mutedBg  = cssVar('--muted', '#efefef');
    const primary  = cssVar('--primary', '#644a40');
    const success  = '#1f7a4d';
    const danger   = cssVar('--destructive', '#e54d2e');

    if (loading) return e('p', { style: { color: muted, padding: '12px 0' } }, 'Cargando clasificación…');
    if (error)   return e('p', { style: { color: danger } }, error);
    if (rows.length === 0) return e('div', {
        style: { background: card, border: '1px solid ' + border, borderRadius: '12px', padding: '20px', textAlign: 'center', color: muted }
    }, 'Aún no hay partidos jugados en este torneo.');

    const cell  = { padding: '12px 14px', textAlign: 'center', borderBottom: '1px solid ' + border, fontSize: '0.88rem', color: fg };
    const hcell = { padding: '10px 14px', textAlign: 'center', background: mutedBg, color: muted, fontWeight: '600', fontSize: '0.72rem', textTransform: 'uppercase', letterSpacing: '0.05em', borderBottom: '1px solid ' + border };
    const hLeft = Object.assign({}, hcell, { textAlign: 'left' });
    const nCell = Object.assign({}, cell, { textAlign: 'left', fontWeight: '600' });
    const pts   = Object.assign({}, cell, { fontWeight: '800', color: fg, fontSize: '0.95rem' });
    const win   = Object.assign({}, cell, { color: success, fontWeight: '600' });
    const lose  = Object.assign({}, cell, { color: danger });

    const gdColor = (v) => v > 0 ? success : v < 0 ? danger : 'inherit';

    const header = e('tr', null,
        e('th', { style: Object.assign({}, hcell, { width: '40px' }) }, '#'),
        e('th', { style: hLeft }, 'Equipo'),
        e('th', { style: hcell, title: 'Partidos jugados' }, 'PJ'),
        e('th', { style: hcell, title: 'Victorias' }, 'V'),
        e('th', { style: hcell, title: 'Empates' }, 'E'),
        e('th', { style: hcell, title: 'Derrotas' }, 'D'),
        e('th', { style: hcell, title: 'Goles a favor' }, 'GF'),
        e('th', { style: hcell, title: 'Goles en contra' }, 'GC'),
        e('th', { style: hcell, title: 'Diferencia de goles' }, 'DG'),
        e('th', { style: hcell, title: 'Puntos' }, 'Pts')
    );

    const bodyRows = rows.map((row, i) =>
        e('tr', {
            key: row.teamId,
            style: {
                background: i % 2 === 0 ? card : mutedBg + '55',
                borderLeft: i === 0 ? '3px solid ' + primary : '3px solid transparent',
                transition: 'background 0.15s ease'
            },
            onMouseEnter: (ev) => { ev.currentTarget.style.background = mutedBg; },
            onMouseLeave: (ev) => { ev.currentTarget.style.background = i % 2 === 0 ? card : mutedBg + '55'; }
        },
            e('td', { style: Object.assign({}, cell, { fontWeight: '700', color: muted }) }, String(i + 1)),
            e('td', { style: nCell }, e('a', { href: '/team/' + row.teamId, style: { color: fg, textDecoration: 'none' } }, row.teamName)),
            e('td', { style: cell }, row.played),
            e('td', { style: win  }, row.won),
            e('td', { style: cell }, row.drawn),
            e('td', { style: lose }, row.lost),
            e('td', { style: cell }, row.goalsFor),
            e('td', { style: cell }, row.goalsAgainst),
            e('td', { style: Object.assign({}, cell, { color: gdColor(row.goalDiff), fontWeight: '600' }) },
                (row.goalDiff > 0 ? '+' : '') + row.goalDiff
            ),
            e('td', { style: pts }, row.points)
        )
    );

    return e('div', { style: { border: '1px solid ' + border, borderRadius: '12px', overflow: 'hidden', background: card, boxShadow: '0 1px 4px rgba(32,32,32,0.06)' } },
        e('div', { style: { overflowX: 'auto' } },
            e('table', { style: { width: '100%', borderCollapse: 'collapse' } },
                e('thead', null, header),
                e('tbody', null, ...bodyRows)
            )
        ),
        e('p', { style: { fontSize: '0.72rem', color: muted, margin: 0, padding: '10px 14px', borderTop: '1px solid ' + border } },
            'PJ = Jugados · V = Victorias · E = Empates · D = Derrotas · GF = Goles favor · GC = Goles contra · DG = Diferencia · Pts = Puntos'
        )
    );
}

document.addEventListener('DOMContentLoaded', function () {
    const container = document.getElementById('standings-root');
    if (container && typeof TOURNAMENT_ID !== 'undefined') {
        ReactDOM.createRoot(container).render(e(StandingsTable, { tournamentId: TOURNAMENT_ID }));
    }
});
