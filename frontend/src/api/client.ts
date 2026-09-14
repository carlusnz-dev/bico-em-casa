export async function apiFetch(url: string, options: RequestInit) {
    const resposta = await fetch(`${process.env.NEXT_PUBLIC_API_URL}${url}`,options);
    console.log('URL: ', url)
    if (resposta.ok) {
        const texto = await resposta.text();

        if (!texto) {
            return null;
        }
        return JSON.parse(texto);
    }
}