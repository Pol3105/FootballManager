const { useState, useEffect } = React;
const e = React.createElement;

function StandingsTable({ tournamentId }) {
    const [rows, setRows]       = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError]     = useState(null);

    useEffect(() => {
        fetch('/api/tournament/' + tournamentId + '/standings')
            .then(r => {
                if (!r.ok) throw new Error("Error al cargar la classifica");
                return r.json();
            })
            .then(data => { setRows(data); setLoading(false); })
            .catch(err => { setError(err.message); setLoading(false); });
    }, [tournamentId]);

    if (loading) return e('p', { style: { color: '#6b7280', padding: '12px 0' } }, 'Cargando classifica…');
    if (error)   return e('p', { style: { color: '#e63946' } }, '⚠️ ' + error);
    if (rows.length === 0) return e('div', {
        style: { background: '#f9fafb', border: '1px solid #e5e7eb', borderRadius: '8px', padding: '20px', textAlign: 'center', color: '#6b7280' }
    }, 'Aún no hay partidos jugados en este torneo.');

    const medal = (i) => i === 0 ? '🥇' : i === 1 ? '🥈' : i === 2 ? '🥉' : String(i + 1);

    const cell   = { padding: '10px 14px', textAlign: 'center', borderBottom: '1px solid #e5e7eb' };
    const hcell  = Object.assign({}, cell, { background: '#1e3a5f', color: 'white', fontWeight: '600', fontSize: '0.8rem', textTransform: 'uppercase', letterSpacing: '0.05em' });
    const hLeft  = Object.assign({}, hcell, { textAlign: 'left' });
    const nCell  = Object.assign({}, cell, { textAlign: 'left', fontWeight: '500' });
    const pts    = Object.assign({}, cell, { fontWeight: '700', color: '#1e3a5f', fontSize: '1rem' });
    const win    = Object.assign({}, cell, { color: '#2ecc71', fontWeight: '600' });
    const lose   = Object.assign({}, cell, { color: '#e63946' });

    const gdColor = (v) => v > 0 ? '#2ecc71' : v < 0 ? '#e63946' : 'inherit';

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
                background: i % 2 === 0 ? 'white' : '#f9fafb',
                borderLeft: i === 0 ? '4px solid #2ecc71' : '4px solid transparent'
            }
        },
            e('td', { style: cell }, medal(i)),
            e('td', { style: nCell }, e('a', { href: '/team/' + row.teamId, style: { color: '#1e3a5f', textDecoration: 'none' } }, row.teamName)),
            e('td', { style: cell }, row.played),
            e('td', { style: win  }, row.won),
            e('td', { style: cell }, row.drawn),
            e('td', { style: lose }, row.lost),
            e('td', { style: cell }, row.goalsFor),
            e('td', { style: cell }, row.goalsAgainst),
            e('td', { style: Object.assign({}, cell, { color: gdColor(row.goalDiff) }) },
                (row.goalDiff > 0 ? '+' : '') + row.goalDiff
            ),
            e('td', { style: pts }, row.points)
        )
    );

    return e('div', { style: { overflowX: 'auto' } },
        e('table', { style: { width: '100%', borderCollapse: 'collapse', background: 'white', borderRadius: '8px', overflow: 'hidden', boxShadow: '0 1px 4px rgba(0,0,0,0.08)' } },
            e('thead', null, header),
            e('tbody', null, ...bodyRows)
        ),
        e('p', { style: { fontSize: '0.75rem', color: '#9ca3af', marginTop: '6px' } },
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
