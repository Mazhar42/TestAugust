import groovy.transform.Field

@Field final List PERCENT_REBATE_TYPES = ["GrowthRebate", "Fixed Percent", "Growth Absolute Percent",
                                        "Growth Percent Percent", "Linear Percent", "Multi Increment Percent",
                                        "Multi Target Percent", "Single Increment Growth Percent", "Single Increment Percent",
                                        "Single Target Percent", "Single Volume Total Percent", "Stepped Percent",
                                        "Supplier Growth Percent Percent", "Supplier Multi Increment Percent"]

@Field final String REBATE_INPUTS_CONFIG_LOGIC = "Configurator_RebateInputs"
@Field final String REBATE_INPUTS_CONFIG_NAME = "Inputs"
@Field final String PRODUCT_REBATE_VOLUME_NAME = "ProductRebateVolume"
@Field final String PRODUCT_REBATE_VOLUME_LABEL = "Product Rebate Volume"
@Field final String CAP_PCT_INPUT_NAME = "Cap %"
@Field final String CAP_CONFIG_NAME = "Configuration"
@Field final String REBATE_INPUT_MATRIX_NAME = "Rebates"
@Field final String REBATE_INPUT_MATRIX_LABEL = "Rebates"

@Field final String ANNUALLY = "Annually"
@Field final String SEMI_ANNUALLY = "Semi-Annually"
@Field final String QUARTERLY = "Quarterly"
@Field final String MONTHLY = "Monthly"

@Field final String BUSINESS_UNIT = "Business Unit"
@Field final String PRODUCT_LINE = "Product Line"

@Field final Map REBATE_VOLUME_MATRIX_FIELDS_CONFIG = ["GLOBAL_PCTR"        : [readOnly: true, label: BUSINESS_UNIT, type: "Text"],
                                                                 "GLOBAL_PRODUCT_NAME": [readOnly: true, label: PRODUCT_LINE, type: "Text"],
                                                                 "MONTH_TARGET"       : [readOnly: true, label: "Month Target in L", type: "Numeric", paymentPeriod: MONTHLY],
                                                                 "HALF_TARGET"        : [readOnly: true, label: "1H Target in L", type: "Numeric", paymentPeriod: SEMI_ANNUALLY],
                                                                 "QUARTER_TARGET"     : [readOnly: true, label: "Quarter Target in L", type: "Numeric", paymentPeriod: QUARTERLY],
                                                                 "ANNUAL_TARGET"      : [readOnly: true, label: "Annual Target in L", type: "Numeric", paymentPeriod: ANNUALLY],
                                                                 "FULL_YEAR_TARGET"   : [readOnly: false, label: "Full Year Target in L", type: "Numeric"],
                                                                 "CAP_TARGET"         : [readOnly: true, label: "Cap Target in L", type: "Numeric"]
]

@Field final Map REBATE_FIELDS_CONFIG = ["GLOBAL_PCTR"        : [readOnly: true, label: BUSINESS_UNIT, type: "Text"],
                                                   "GLOBAL_PRODUCT_NAME": [readOnly: true, label: PRODUCT_LINE, type: "Text"],
                                                   "BASIC"              : [readOnly: false, label: "Basic %", type: "Numeric"],
                                                   "GROWTH"             : [readOnly: false, label: "Growth %", type: "Numeric"]
]

@Field final Map FACTORS_MAP = [(ANNUALLY): 1, (SEMI_ANNUALLY): 2, (QUARTERLY): 4, (MONTHLY): 12]