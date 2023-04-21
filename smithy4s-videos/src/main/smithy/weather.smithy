$version: "2"

namespace hello

use alloy#simpleRestJson

@simpleRestJson
service WeatherService {
    operations: [GetWeather, CreateCity]
}

@http(method: "GET", uri: "/cities/{cityId}/weather")
@readonly
operation GetWeather {
    input := {
        @required
        @httpLabel
        cityId: CityId
    }
    output := {
        @required
        weather: String
        degrees: Integer
    }
}

@http(method: "POST", uri: "/cities", code: 201)
operation CreateCity {
    input := {
        @required
        city: String
        @required
        country: String
    }
    output := {
        @required
        cityId: CityId
    }
}

string CityId
