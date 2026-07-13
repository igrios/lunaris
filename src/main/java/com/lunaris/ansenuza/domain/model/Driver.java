package com.lunaris.ansenuza.domain.model;                                                                                                            
                                                                                                                                                          
    import jakarta.persistence.*;                                                                                                                         
    import lombok.AllArgsConstructor;                                                                                                                     
    import lombok.Data;                                                                                                                                   
    import lombok.NoArgsConstructor;                                                                                                                      
    import java.util.UUID;                                                                                                                                
                                                                                                                                                          
    @Entity                                                                                                                                               
    @Table(name = "drivers")                                                                                                                              
    @Data                                                                                                                                                 
    @NoArgsConstructor                                                                                                                                    
    @AllArgsConstructor                                                                                                                                   
    public class Driver {                                                                                                                                 
                                                                                                                                                          
        @Id                                                                                                                                               
        private UUID id;                                                                                                                                  
                                                                                                                                                          
        @Column(name = "full_name", nullable = false)                                                                                                     
        private String fullName;                                                                                                                          
                                                                                                                                                          
        @Column(name = "phone", nullable = false)                                                                                                         
        private String phone;                                                                                                                             
                                                                                                                                                          
        @Column(name = "active")                                                                                                                          
        private boolean active;                                                                                                                           
                                                                                                                                                          
        @Column(name = "ranking")                                                                                                                         
        private Integer ranking;                                                                                                                          
    }              