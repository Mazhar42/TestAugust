def status = out.RR?.status

if (out.RR?.status != "APPROVED") {
    return
}
api.logInfo("ncb@@ RR", out.RR)
//String rebateType = out.RR?.rebateType
def rrUN = out.RR?.uniqueName
def name = out.RR?.name
def label = out.RR?.label
def startDate = out.StartDate
def endDate = out.EndDate
def payoutDate = out.RR?.payoutDate

def newPayoutRecordCreated = false

/*Map pyrs = api.find("PYR", Filter.equal("rebateRecordUN", rrUN))?.groupBy({ it.rebateRecordUN }, { it.name })
api.logInfo("ncb@@ pyrs", pyrs)*/
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
api.logInfo("ncb@@ pRecord", pRecord)

payoutRecords.addOrUpdate("Payout", store, pRecord)