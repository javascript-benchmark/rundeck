import axios from 'axios';
import _ from 'lodash';
import Trellis from '@rundeck/ui-trellis';
import TourConstants from '@/components/tour/constants';
export const getTours = () => {
    let tours = [];
    return new Promise((resolve, reject) => {
        axios.get(TourConstants.tourManifestUrl).then((response) => {
            if (response && response.data && response.data.length) {
                _.each(response.data, (tourLoader) => {
                    tours.push(tourLoader);
                });
                resolve(tours);
            }
        }).catch(function (error) {
            reject(new Error(error));
            console.log('Tour manifest not found');
        });
    });
};
export const getTour = (tourLoader, tourKey) => {
    return new Promise((resolve, reject) => {
        axios.get(`${TourConstants.tourUrl}${tourLoader}/${tourKey}.json`)
            .then((response) => {
            if (response && response.data) {
                resolve(response.data);
            }
        })
            .catch(function (error) {
            console.log(error);
            reject(error);
        });
    });
};
export const unsetTour = () => {
    return new Promise((resolve) => {
        Trellis.FilterPrefs.unsetFilterPref('activeTour').then(() => {
            Trellis.FilterPrefs.unsetFilterPref('activeTourStep').then(() => {
                resolve();
            });
        });
    });
};
export default {
    getTours,
    getTour,
    unsetTour
};
//# sourceMappingURL=services.js.map