def item = api.currentItem()

if (item?.status != "APPROVED") {
    return
}


String rebateType = item?.rebateType
def rrUN = item?.uniqueName
def name = item?.name
def label = item?.label
def startDate = item.StartDate
def endDate = item.EndDate
def payoutDate = item?.payoutDate

def newPayoutRecordCreated = false


def store = rrUN + startDate

def pRecord = [
        rebateRecordUN: rrUN,
        name          : name,
        status        : status,
        label         : label,
        startDate     : "2023-01-01",
        endDate       : "2023-10-31",
        payoutDate    : payoutDate,
        //attribute1    : product,
        //attribute2    : "CID-0001",
        //attribute3    : rebateValue,
]

payoutRecords.addOrUpdate("Payout", store, pRecord)