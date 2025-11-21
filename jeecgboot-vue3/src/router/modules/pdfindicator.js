import { t } from '@/hooks/web/useI18n'
import { LAYOUT } from '@/router/constant'
import { findPath } from '@/utils/helper/menuHelper'
import { getParentLayout } from '@/utils/routerHelper'

const routeName = 'pdfindicator'

const routes = [
  {
    path: '/pdfindicator',
    name: routeName,
    component: LAYOUT,
    redirect: '/pdfindicator/pdfIndicatorTask',
    meta: {
      title: 'PDF指标抽取',
      icon: 'mdi:file-pdf-box',
      orderNo: 100000,
    },
    children: [
      {
        path: 'pdfIndicatorTask',
        name: `${routeName}.pdfIndicatorTask`,
        component: () => import('@/views/pdfindicator/pdfIndicatorTask.vue'),
        meta: {
          title: '任务管理',
          icon: 'mdi:file-document-outline',
        },
      },
      {
        path: 'pdfDocument',
        name: `${routeName}.pdfDocument`,
        component: () => import('@/views/pdfindicator/pdfDocument.vue'),
        meta: {
          title: '文档管理',
          icon: 'mdi:file-pdf-outline',
        },
      },
    ],
  },
]

export default routes
