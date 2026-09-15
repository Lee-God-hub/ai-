export const PROPERTY_TYPES = {
  RESIDENTIAL: 0,
  APARTMENT: 1,
  VILLA: 2
}

export const PROPERTY_TYPE_LIST = [
  { label: '住宅', value: PROPERTY_TYPES.RESIDENTIAL },
  { label: '公寓', value: PROPERTY_TYPES.APARTMENT },
  { label: '别墅', value: PROPERTY_TYPES.VILLA }
]

export const PROPERTY_TYPE_TEXTS: Record<number, string> = {
  [PROPERTY_TYPES.RESIDENTIAL]: '住宅',
  [PROPERTY_TYPES.APARTMENT]: '公寓',
  [PROPERTY_TYPES.VILLA]: '别墅'
}

export const TRANSACTION_TYPES = {
  RENT: 0,
  SALE: 1
}

export const TRANSACTION_TYPE_LIST = [
  { label: '出租', value: TRANSACTION_TYPES.RENT },
  { label: '出售', value: TRANSACTION_TYPES.SALE }
]

export const TRANSACTION_TYPE_TEXTS: Record<number, string> = {
  [TRANSACTION_TYPES.RENT]: '出租',
  [TRANSACTION_TYPES.SALE]: '出售'
}

export const PROPERTY_STATUS = {
  PENDING: 0,
  APPROVED: 1,
  REJECTED: 2,
  ONLINE: 3,
  OFFLINE: 4
}

export const PROPERTY_STATUS_TEXTS: Record<number, string> = {
  [PROPERTY_STATUS.PENDING]: '待审核',
  [PROPERTY_STATUS.APPROVED]: '审核通过',
  [PROPERTY_STATUS.REJECTED]: '已驳回',
  [PROPERTY_STATUS.ONLINE]: '已上架',
  [PROPERTY_STATUS.OFFLINE]: '已下架'
}

export const PROPERTY_STATUS_TYPES: Record<number, string> = {
  [PROPERTY_STATUS.PENDING]: 'info',
  [PROPERTY_STATUS.APPROVED]: 'success',
  [PROPERTY_STATUS.REJECTED]: 'danger',
  [PROPERTY_STATUS.ONLINE]: 'success',
  [PROPERTY_STATUS.OFFLINE]: 'info'
}