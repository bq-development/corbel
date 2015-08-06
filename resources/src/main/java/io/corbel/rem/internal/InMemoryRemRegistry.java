package io.corbel.rem.internal;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

import io.corbel.resources.rem.Rem;
import io.corbel.resources.rem.RemRegistry;
import io.corbel.resources.rem.model.RemDescription;

/**
 * In memory implementation of a REM registryLookUp.
 * 
 * URI Patterns are stored as partial order where P1 bigger than P2 if P1.match(P2).
 * 
 * Media types are stored as partial order where T1 bigger than T2 if T1.includes(T2)
 * 
 * @author Alexander De Leon
 * 
 */
public class InMemoryRemRegistry implements RemRegistry {

    private final SortedSet<UriPatternRegistryEntry> registry = new TreeSet<>();
    private static final Logger LOG = LoggerFactory.getLogger(InMemoryRemRegistry.class);

    private final Map<String, UriPatternRegistryEntry> registryLookUp = new HashMap<>();

    @Override
    public Rem getRem(String uri, List<MediaType> acceptableMediaTypes, HttpMethod method, List<Rem> remsExcluded) {
        for (UriPatternRegistryEntry entry : registry) {
            if (entry.matches(uri)) {
                for (MediaType mediaType : acceptableMediaTypes) {
                    MediaTypeRegistryEntry mediaTypeRegistryEntry = entry.get(mediaType);
                    if (mediaTypeRegistryEntry != null) {
                        Rem rem = mediaTypeRegistryEntry.get(method);
                        if (rem != null && (remsExcluded == null || !remsExcluded.contains(rem))) {
                            return rem;
                        }
                    }
                }
            }
        }
        LOG.info("No REM found for URI={}, MediaTypes={}, Method={}", uri, acceptableMediaTypes, method);
        return null;
    }

    @Override
    public List<RemDescription> getRegistryDescription() {
        List<RemDescription> descriptionsList = new LinkedList<>();
        for (UriPatternRegistryEntry uriPatternRegistryEntry : registry) {
            for (MediaTypeRegistryEntry mediaTypeRegistryEntry : uriPatternRegistryEntry.getMediaTypeRegistryEntries()) {
                Map<HttpMethod, Rem> remRegistry = mediaTypeRegistryEntry.getRemRegistry();
                for (HttpMethod httpMethod : remRegistry.keySet()) {
                    RemDescription remDescription = new RemDescription(remRegistry.get(httpMethod).getClass().getName(), httpMethod.name(),
                            mediaTypeRegistryEntry.getMediaType().toString(), uriPatternRegistryEntry.getUriPattern().pattern());

                    descriptionsList.add(remDescription);
                }
            }
        }

        return descriptionsList;
    }

    @Override
    public void registerRem(Rem rem, String uriPattern, MediaType mediaType, HttpMethod... methods) {
        HttpMethod[] methodsToAdd = (methods == null || methods.length == 0) ? HttpMethod.values() : methods;

        UriPatternRegistryEntry registryEntry = registryLookUp.get(uriPattern);
        if (registryEntry == null) {
            registryEntry = new UriPatternRegistryEntry(uriPattern);
            registryLookUp.put(uriPattern, registryEntry);
            registryEntry.add(rem, mediaType, methodsToAdd);
            registry.add(registryEntry);
        } else {
            registryEntry.add(rem, mediaType, methodsToAdd);
        }
        LOG.info("Registered REM {}: URI={} , MediaType={}, Methods={}", rem, uriPattern, mediaType.toString(),
                Arrays.toString(methodsToAdd));
    }

    @Override
    public void registerRem(Rem rem, String uriPattern, HttpMethod... methods) {
        registerRem(rem, uriPattern, MediaType.ALL, methods);
    }

    @Override
    public Rem getRem(String name) {
        for (UriPatternRegistryEntry entry : registry) {
            for (MediaTypeRegistryEntry mediaTypeRegistry : entry.getMediaTypeRegistryEntries()) {
                for (Rem rem : mediaTypeRegistry.getRems()) {
                    if (rem.getClass().getSimpleName().equals(name)) {
                        return rem;
                    }
                }
            }
        }
        return null;
    }

    private class MediaTypeRegistryEntry implements Comparable<MediaTypeRegistryEntry> {
        private final MediaType mediaType;
        private final Map<HttpMethod, Rem> remRegistry = new HashMap<>();

        public MediaTypeRegistryEntry(MediaType mediaType) {
            this.mediaType = mediaType;
        }

        public boolean matches(MediaType obj) {
            return this.mediaType.includes(obj);
        }

        public Rem get(HttpMethod method) {
            return remRegistry.get(method);
        }

        public void add(Rem rem, HttpMethod[] methods) {
            for (HttpMethod method : methods) {
                remRegistry.put(method, rem);
            }
        }

        public Collection<Rem> getRems() {
            return remRegistry.values();
        }

        public Map<HttpMethod, Rem> getRemRegistry() {
            return remRegistry;
        }

        public MediaType getMediaType() {
            return mediaType;
        }

        @Override
        public int compareTo(MediaTypeRegistryEntry o) {
            return this.mediaType.compareTo(o.mediaType);
        }
    }

    private class UriPatternRegistryEntry implements Comparable<UriPatternRegistryEntry> {
        private final Pattern uriPattern;
        private final SortedSet<MediaTypeRegistryEntry> mediaTypeRegistry = new TreeSet<MediaTypeRegistryEntry>(Collections.reverseOrder());
        private final Map<MediaType, MediaTypeRegistryEntry> mediaTypeRegistryLookUp = new HashMap<>();

        public UriPatternRegistryEntry(String uriPattern) {
            this(uriPattern == null ? null : Pattern.compile(uriPattern));
        }

        public UriPatternRegistryEntry(Pattern uriPattern) {
            if (uriPattern == null) {
                throw new IllegalArgumentException("Invalid uri pattern: null");
            }
            this.uriPattern = uriPattern;
        }

        public boolean matches(String uri) {
            return uriPattern.matcher(uri).matches();
        }

        public void add(Rem rem, MediaType mediaType, HttpMethod[] methods) {
            MediaTypeRegistryEntry entry = mediaTypeRegistryLookUp.get(mediaType);
            if (entry == null) {
                entry = new MediaTypeRegistryEntry(mediaType);
                mediaTypeRegistry.add(entry);
                mediaTypeRegistryLookUp.put(mediaType, entry);
            }
            entry.add(rem, methods);
        }

        public MediaTypeRegistryEntry get(MediaType mediaType) {
            for (MediaTypeRegistryEntry entry : mediaTypeRegistry) {
                if (entry.matches(mediaType)) {
                    return entry;
                }
            }
            return null;
        }

        public Collection<MediaTypeRegistryEntry> getMediaTypeRegistryEntries() {
            return mediaTypeRegistry;
        }

        public Pattern getUriPattern() {
            return uriPattern;
        }

        @Override
        public int compareTo(UriPatternRegistryEntry o) {
            if (this.uriPattern.matcher(o.uriPattern.pattern()).matches()) {
                if (o.uriPattern.matcher(this.uriPattern.pattern()).matches()) {
                    // equals
                    if (!this.mediaTypeRegistry.isEmpty()) {
                        return o.mediaTypeRegistry.first().compareTo(this.mediaTypeRegistry.first());
                    } else {
                        return 0;
                    }
                }
                return 1;
            }
            return -1;
        }
    }
}
