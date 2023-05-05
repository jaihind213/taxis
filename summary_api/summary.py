from pydantic import BaseModel


class Summary:

    def __init__(self, company):
        self.company = company
        pass

    @property
    def num_trips(self):
        return self._num_trips

    @num_trips.setter
    def num_trips(self, value):
        self._num_trips = value

    @property
    def uniq_taxis_seen(self):
        return self._uniq_taxis_seen

    @uniq_taxis_seen.setter
    def uniq_taxis_seen(self, value):
        self._uniq_taxis_seen = value

    @property
    def avg_trip_time(self):
        return self._avg_trip_time

    @avg_trip_time.setter
    def avg_trip_time(self, value):
        self._avg_trip_time = value

    @property
    def total_earning(self):
        return self._total_earning

    @total_earning.setter
    def earning(self, value):
        self._total_earning = value

    @property
    def avg_trip_fare(self):
        return self._avg_trip_fare

    @avg_trip_fare.setter
    def avg_trip_fare(self, value):
        self._avg_trip_fare = value

    def toJSON(self):
        return {'company': self.company, 'num_trips': self._num_trips, 'uniq_taxis_seen': self._uniq_taxis_seen,
                'avg_trip_time': self._avg_trip_time, 'avg_trip_fare': self._avg_trip_fare,
                'total_earning': self._total_earning}


class AreaSummary:
    def __init__(self, company, area: int, num_trips, to_from: str):
        self.company = company
        self.area = area
        self.num_trips = num_trips
        self.to_from = to_from  # either 'to' or 'from'


class Filter(BaseModel):
    where_clause: str
    class Config:
        schema_extra = {
            "example": {
                "where_clause": "Foo",
                "description": "A very nice Item",
                "price": 35.4,
                "tax": 3.2,
            }
        }
