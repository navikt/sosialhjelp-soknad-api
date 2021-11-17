//package no.nav.sosialhjelp.soknad.consumer.pdl.adressesok.dto;
//
//
//import com.fasterxml.jackson.annotation.JsonCreator;
//import com.fasterxml.jackson.annotation.JsonProperty;
//
//import java.util.List;
//
//public class AdresseSokResult {
//
//    private final List<AdresseSokHit> hits;
//    private final int pageNumber;
//    private final int totalPages;
//    private final int totalHits;
//
//    @JsonCreator
//    public AdresseSokResult(
//            @JsonProperty("hits") List<AdresseSokHit> hits,
//            @JsonProperty("pageNumber") int pageNumber,
//            @JsonProperty("totalPages") int totalPages,
//            @JsonProperty("totalHits") int totalHits
//    ) {
//        this.hits = hits;
//        this.pageNumber = pageNumber;
//        this.totalPages = totalPages;
//        this.totalHits = totalHits;
//    }
//
//    public List<AdresseSokHit> getHits() {
//        return hits;
//    }
//
//    public int getPageNumber() {
//        return pageNumber;
//    }
//
//    public int getTotalPages() {
//        return totalPages;
//    }
//
//    public int getTotalHits() {
//        return totalHits;
//    }
//}
