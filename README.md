# InteRxactions
A direct continuation of the Android application [RxWatch](https://github.com/osu-cs492-599-w24/final-project-rxwatch) built for  [CS 492/599 – Mobile Software Development](https://web.engr.oregonstate.edu/~hessro/teaching/cs492-w24) at Oregon State University with Prof. Rob Hess, Winter 2024.

## High-level overview
InteRxactions focuses on providing users accessible information about drug interactions and adverse
drug effects for any medication that they may currently be using. The app provides a search box
where they can enter the name of the medication for which they are seeking information and allows
them to fine tune those results based on filters. 

The application has multiple screens. The main screen includes a search bar and button 
along with a display of the results after a search is initiated. The other screens contain
more in-depth information about the results of the search.

All the information that is displayed in the application is taken from the
[OpenFDA API]([url](https://open.fda.gov/)) which is approved and updated by the FDA.

The OpenFDA API does not require a registered account or key. 

## Third-party API calls
- Drug Labeling endpoint: https://open.fda.gov/apis/drug/label/ 
- Adverse Effects endpoint: https://open.fda.gov/apis/drug/event/ 
