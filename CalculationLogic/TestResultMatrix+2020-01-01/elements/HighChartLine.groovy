def data = out.queryProduct
def categoryData =  data?.keySet()//list of xAxis values
// TODO: provide real data in out.Data
def seriesData = [
        [
                ProductId: 'Tokyo', //name of the series
                data: data?.values()
        ]
]

def definition = [
        chart      : [
                type: 'column',
        ],

        title      : [
                text: 'Title',
        ],

        subtitle   : [
                text: 'Subtitle',
        ],

        plotOptions: [
                line: [
                        dataLabels         : [
                                enabled: true,
                        ],
                        enableMouseTracking: false,
                ],
        ],

        series     : seriesData,

        xAxis      : [
                categories: categoryData,
        ],

        yAxis      : [
                title: [
                        text: 'Y-axis Title',
                ]
        ],
]

return api.buildHighchart(definition)
