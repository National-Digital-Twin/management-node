/*
 * SPDX-License-Identifier: Apache-2.0
 * © Crown Copyright 2026. This work has been developed by the National Digital Twin Programme and is legally
 * attributed to the Department for Business and Trade (UK) as the governing entity.
 */

package uk.gov.dbt.ndtp.ia.node.management.service.data;

import java.util.List;
import uk.gov.dbt.ndtp.ia.node.management.model.dto.ProductDTO;
import uk.gov.dbt.ndtp.ia.node.management.model.dto.ProductDiscoveryResponseDTO;

/**
 * Runs product discovery: queries candidate products matching the requester's search
 * criteria, then applies per-candidate PDP authorisation, keeping only the products the
 * requester is authorised to discover.
 */
public interface ProductDiscoveryService {

    /**
     * Queries discovery candidates matching the given search criteria, then evaluates one
     * PDP decision per candidate, keeping only the ALLOWed ones.
     *
     * @param clientId identity of the calling client
     * @param organisation organisation the client belongs to, if known
     * @param name optional case-insensitive contains filter on product name
     * @param topic optional case-insensitive contains filter on product topic
     * @param type optional case-insensitive exact filter on product type name
     * @return the products the requester is authorised to discover, matching the criteria
     */
    ProductDiscoveryResponseDTO discover(String clientId, String organisation, String name, String topic, String type);

    /**
     * Evaluates one PDP decision per candidate product and returns only the ALLOWed ones. A
     * candidate is excluded (not the whole request failed) if the PDP denies it or the PDP
     * call itself fails, so a partial PDP outage degrades results rather than the request.
     *
     * @param clientId identity of the calling client
     * @param organisation organisation the client belongs to, if known
     * @param candidates discovery candidate products to authorise
     * @return the subset of candidates the PDP allows for this requester
     */
    List<ProductDTO> filterAuthorised(String clientId, String organisation, List<ProductDTO> candidates);
}
