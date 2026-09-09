export async function apiFetch(url, options) {
    const resposta = await fetch(`${process.env.NEXT_PUBLIC_API_URL}${url}`,options);
    console.log('URL: ', url)
    if (resposta.ok) {
        return resposta.json();
    }
}