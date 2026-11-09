import { listarTags } from '@/api/tags';
import { EditarServico } from '@/components/forms/EditarServico';

export const dynamic = 'force-dynamic';

export default async function EditarServicoPage({ params }: { params: Promise<{ id: string }> }) {
  const [{ id }, tags] = await Promise.all([params, listarTags()]);
  return <EditarServico id={id} tags={tags} />;
}
