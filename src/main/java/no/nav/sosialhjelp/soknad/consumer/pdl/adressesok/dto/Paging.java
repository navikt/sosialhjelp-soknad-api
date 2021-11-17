//package no.nav.sosialhjelp.soknad.consumer.pdl.adressesok.dto;
//
//import java.util.List;
//
//public class Paging {
//    private final Integer pageNumber;
//    private final Integer resultsPerPage;
//    private final List<SortBy> sortBy;
//
//    public Paging(
//            Integer pageNumber,
//            Integer resultsPerPage,
//            List<SortBy> sortBy
//    ) {
//        this.pageNumber = pageNumber;
//        this.resultsPerPage = resultsPerPage;
//        this.sortBy = sortBy;
//    }
//
//    public Integer getPageNumber() {
//        return pageNumber;
//    }
//
//    public Integer getResultsPerPage() {
//        return resultsPerPage;
//    }
//
//    public List<SortBy> getSortBy() {
//        return sortBy;
//    }
//
//    public static class SortBy {
//        private final String fieldName;
//        private final Direction direction;
//
//
//        public SortBy(
//                String fieldName,
//                Direction direction
//        ) {
//            this.fieldName = fieldName;
//            this.direction = direction;
//        }
//
//        public String getFieldName() {
//            return fieldName;
//        }
//
//        public Direction getDirection() {
//            return direction;
//        }
//    }
//}
