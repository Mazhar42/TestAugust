
def item = api.currentItem()

api.addOrUpdate("PX6",[
        "name": "InvoicePriceHistory",
        "sku": item?.sku,
        "lastUpdateDate": item?.lastUpdateDate,
        "attribute1":item?.label,
        "attribute2": item?.resultPrice,
        "attribute3": item?.submittedByName,
])