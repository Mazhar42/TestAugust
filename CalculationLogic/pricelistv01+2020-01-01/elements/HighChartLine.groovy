def categoryData = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'] //list of xAxis values
// TODO: provide real data in out.Data
def seriesData = [
        [
                name: 'Tokyo', //name of the series
                data: [7.0, 6.9, 9.5, 14.5, 18.4, 21.5, 25.2, 26.5, 23.3, 18.3, 13.9, 9.6] //list of yAxis values, or list of maps with y value
        ], [
                name: 'London',
                data: [3.9, 4.2, 5.7, 8.5, 11.9, 15.2, 17.0, 16.6, 14.2, 10.3, 6.6, 4.8]
        ],
]

def definition = [
        chart      : [
                type: 'line',
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
